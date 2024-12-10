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
    --algorithm-specification TrainingImage="${FULL_IMAGE_NAME}",TrainingInputMode="File" \
    --role-arn "${ROLE_ARN}" \
    --resource-config InstanceCount=1,InstanceType="ml.m5.large",VolumeSizeInGB=30 \
    --input-data-config "[{
        \"ChannelName\": \"training\",
        \"DataSource\": {
            \"S3DataSource\": {
                \"S3DataType\": \"S3Prefix\",
                \"S3Uri\": \"s3://${S3_BUCKET}/training-data\",
                \"S3DataDistributionType\": \"FullyReplicated\"
            }
        }
    }]" \
    --output-data-config S3OutputPath="s3://${S3_BUCKET}/models" \
    --stopping-condition MaxRuntimeInSeconds=3600

echo "Training job ${TRAINING_JOB_NAME} started"
echo "You can monitor the training job status using:"
echo "aws sagemaker describe-training-job --training-job-name ${TRAINING_JOB_NAME}"
