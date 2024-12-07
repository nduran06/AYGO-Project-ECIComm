#!/bin/bash

# Configuration
BUCKET_NAME="ecicommsagemakerbucket"
LOCAL_DATA_DIR="resources"
S3_PREFIX="training-data"

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "AWS CLI is not installed. Please install it first."
    exit 1
fi

# Check if the local directory exists
if [ ! -d "$LOCAL_DATA_DIR" ]; then
    echo "Local directory $LOCAL_DATA_DIR not found."
    exit 1
fi

# Create the bucket if it doesn't exist
aws s3 mb s3://$BUCKET_NAME --region us-west-2

# Upload all CSV files
echo "Uploading files to S3..."
aws s3 cp $LOCAL_DATA_DIR s3://$BUCKET_NAME/$S3_PREFIX --recursive --exclude "*" --include "*.csv"

# Verify the upload
echo "Verifying uploaded files..."
aws s3 ls s3://$BUCKET_NAME/$S3_PREFIX/

echo "Upload complete!"
