#!/bin/bash
set -e

# Configuration
REGION="us-east-1"
ACCOUNT_ID="645349541441"
REPOSITORY_NAME="ecicomm-recommendation-model"
IMAGE_TAG="latest"
FULL_IMAGE_NAME="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/${REPOSITORY_NAME}:${IMAGE_TAG}"

# SageMaker Configuration
TRAINING_JOB_NAME="recommendation-model-training-$(date +%Y%m%d-%H%M%S)"
ROLE_ARN="arn:aws:iam::645349541441:role/LabRole"  # Your SageMaker role from CDK stack
S3_BUCKET="ecicommsagemakerbucket"  # Your bucket name from CDK stack

check_s3_data() {
    echo "Checking training data in S3..."
    required_files=(
        "user_profiles.csv"
        "products.csv"
        "user_behavior.csv"
        "orders.csv"
        "order_items.csv"
        "product_recommendations.csv"
    )

    for file in "${required_files[@]}"; do
        if ! aws s3 ls "s3://${S3_BUCKET}/training-data/${file}" >/dev/null 2>&1; then
            echo "Error: Missing required file: ${file}"
            exit 1
        fi
    done
    echo "All required training files present in S3"
}

check_s3_data

echo "Creating/verifying ECR repository..."
aws ecr describe-repositories --repository-names ${REPOSITORY_NAME} --region ${REGION} || \
    aws ecr create-repository --repository-name ${REPOSITORY_NAME} --region ${REGION}

echo "Logging into ECR..."
aws ecr get-login-password --region $REGION | podman login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com

echo "Building Docker image with V2 manifest format..."
podman build --format docker --platform=linux/amd64 -t ${REPOSITORY_NAME}:${IMAGE_TAG} .

echo "Tagging image..."
podman tag ${REPOSITORY_NAME}:${IMAGE_TAG} ${FULL_IMAGE_NAME}

echo "Pushing to ECR..."
podman push ${FULL_IMAGE_NAME}

echo "Verifying image in ECR..."
aws ecr describe-images --repository-name ${REPOSITORY_NAME} --image-ids imageTag=${IMAGE_TAG}

echo "Image successfully built and pushed to ECR"
echo "Image URI: ${FULL_IMAGE_NAME}"


echo "Starting SageMaker training job..."
aws sagemaker create-training-job \
    --training-job-name "${TRAINING_JOB_NAME}" \
    --algorithm-specification "{\"TrainingImage\": \"${FULL_IMAGE_NAME}\", \"TrainingInputMode\": \"File\"}" \
    --role-arn "${ROLE_ARN}" \
    --resource-config "{\"InstanceCount\": 1, \"InstanceType\": \"ml.m5.large\", \"VolumeSizeInGB\": 30}" \
    --input-data-config "[{\"ChannelName\": \"training\", \"DataSource\": {\"S3DataSource\": {\"S3DataType\": \"S3Prefix\", \"S3Uri\": \"s3://${S3_BUCKET}/training-data\", \"S3DataDistributionType\": \"FullyReplicated\"}}, \"ContentType\": \"text/csv\"}]" \
    --output-data-config "{\"S3OutputPath\": \"s3://${S3_BUCKET}/models\"}" \
    --stopping-condition "{\"MaxRuntimeInSeconds\": 3600}"

# Add immediate status check
echo "Checking initial job status..."
sleep 10  # Wait a bit for job to start
aws sagemaker describe-training-job \
    --training-job-name "${TRAINING_JOB_NAME}" \
    --query 'TrainingJobStatus' \
    --output text || echo "Failed to get job status"

echo "Training job ${TRAINING_JOB_NAME} started"
echo "You can monitor the training job status using:"
echo "aws sagemaker describe-training-job --training-job-name ${TRAINING_JOB_NAME}"

# Add these lines after the training job creation
echo "Waiting for training job to complete..."
aws sagemaker wait training-job-completed-or-stopped --training-job-name "${TRAINING_JOB_NAME}"

echo "Creating model in SageMaker..."
MODEL_NAME="recommendation-model-$(date +%Y%m%d-%H%M%S)"
MODEL_DATA_URL="s3://${S3_BUCKET}/models/${TRAINING_JOB_NAME}/output/model.tar.gz"

aws sagemaker create-model \
    --model-name "${MODEL_NAME}" \
    --primary-container Image="${FULL_IMAGE_NAME}",ModelDataUrl="${MODEL_DATA_URL}" \
    --execution-role-arn "${ROLE_ARN}"

echo "Creating endpoint configuration..."
aws sagemaker create-endpoint-config \
    --endpoint-config-name "${MODEL_NAME}-config" \
    --production-variants VariantName=AllTraffic,ModelName="${MODEL_NAME}",InstanceType=ml.t2.medium,InitialInstanceCount=1

echo "Creating endpoint..."
aws sagemaker create-endpoint \
    --endpoint-name "${MODEL_NAME}-endpoint" \
    --endpoint-config-name "${MODEL_NAME}-config"

echo "Waiting for endpoint deployment..."
aws sagemaker wait endpoint-in-service --endpoint-name "${MODEL_NAME}-endpoint"
