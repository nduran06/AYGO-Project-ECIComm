#!/bin/bash
set -e

# Configuration
REGION="us-east-1"
ACCOUNT_ID="645349541441"
REPOSITORY_NAME="ecicomm-recommendation-model"
IMAGE_TAG="latest"
FULL_IMAGE_NAME="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/${REPOSITORY_NAME}:${IMAGE_TAG}"

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
