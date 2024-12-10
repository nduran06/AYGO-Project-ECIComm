#!/usr/bin/env python3

class Config:
    # AWS General Settings
    REGION = "us-east-1"

    # ECR Settings
    ECR_REPOSITORY_NAME = "ecicomm-recommendation-model"
    ECR_IMAGE_TAG = "latest"
    ECR_IMAGE_URI = "645349541441.dkr.ecr.us-east-1.amazonaws.com/ecicomm-recommendation-model:latest"

    # IAM Settings
    IAM_ROLE_NAME = "arn:aws:iam::645349541441:role/LabRole"

    # S3 Settings
    S3_BUCKET_NAME = "ecicommsagemakerbucket"
    FOLDER_PATH = "training-data/"
    S3_MODEL_PATH = "models/model.tar.gz"
    DATA_PATH = {
        'user_profiles': FOLDER_PATH + 'user_profiles.csv',
        'products': FOLDER_PATH + 'products.csv',
        'user_behavior': FOLDER_PATH + 'user_behavior.csv',
        'orders': FOLDER_PATH + 'orders.csv',
        'recommendations': FOLDER_PATH + 'product_recommendations.csv'
    }

    # SageMaker Settings
    SAGEMAKER_MODEL_NAME = "recommendation-model"
    SAGEMAKER_ENDPOINT_CONFIG_NAME = "recommendation-endpoint-config"
    SAGEMAKER_ENDPOINT_NAME = "recommendation-endpoint"
    SAGEMAKER_INSTANCE_TYPE = "ml.m5.large"
    SAGEMAKER_INSTANCE_COUNT = 1
    SAGEMAKER_FRAMEWORK_VERSION = "0.23-1"

    # Resource Management
    REMOVE_RESOURCES_ON_DESTROY = False

    # Training Settings
    FEATURES = ['age_group', 'gender', 'location', 'preferred_categories',
                'avg_order_value', 'total_orders']
    TARGET = 'last_purchase_date'

    # Tags
    TAGS = {
        "Environment": "production",
        "Project": "recommendation-system",
        "ManagedBy": "CDK"
    }
