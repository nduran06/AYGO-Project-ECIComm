from constructs import Construct
from aws_cdk import (
    Stack as cdk_Stack,
    aws_sagemaker as sagemaker,
    aws_iam as iam,
    aws_ecr as ecr,
    aws_s3 as s3,
    CfnOutput,
    RemovalPolicy,
    Tags,
)
from config import Config


class SageMakerStack(cdk_Stack):
    def __init__(self, scope: Construct, construct_id: str, **kwargs) -> None:
        super().__init__(scope, construct_id, **kwargs)

        removal_policy = RemovalPolicy.DESTROY if Config.REMOVE_RESOURCES_ON_DESTROY else RemovalPolicy.RETAIN

        try:
            repository = ecr.Repository.from_repository_name(
                self, "ExistingModelRepository",
                repository_name=Config.ECR_REPOSITORY_NAME)
            print(f"Using existing ECR repository: {Config.ECR_REPOSITORY_NAME}")
        except:
            repository = ecr.Repository(
                self, "ModelRepository",
                repository_name=Config.ECR_REPOSITORY_NAME,
                removal_policy=removal_policy)
            print(f"Created new ECR repository: {Config.ECR_REPOSITORY_NAME}")

        try:
            model_bucket = s3.Bucket.from_bucket_name(
                self, "ExistingModelBucket",
                bucket_name=Config.S3_BUCKET_NAME)
            print(f"Using existing S3 bucket: {Config.S3_BUCKET_NAME}")
        except:
            model_bucket = s3.Bucket(
                self, "ModelArtifactsBucket",
                bucket_name=Config.S3_BUCKET_NAME,
                removal_policy=removal_policy,
                auto_delete_objects=Config.REMOVE_RESOURCES_ON_DESTROY)
            print(f"Created new S3 bucket: {Config.S3_BUCKET_NAME}")

        model = sagemaker.CfnModel(
            self, "RecommendationModel",
            model_name=Config.SAGEMAKER_MODEL_NAME,
            execution_role_arn=Config.IAM_ROLE_NAME,
            primary_container=sagemaker.CfnModel.ContainerDefinitionProperty(
                image=Config.ECR_IMAGE_URI,
                model_data_url=f"s3://{model_bucket.bucket_name}/{Config.S3_MODEL_PATH}")
        )

        endpoint_config = sagemaker.CfnEndpointConfig(
            self, "RecommendationEndpointConfig",
            endpoint_config_name=Config.SAGEMAKER_ENDPOINT_CONFIG_NAME,
            production_variants=[
                sagemaker.CfnEndpointConfig.ProductionVariantProperty(
                    initial_instance_count=Config.SAGEMAKER_INSTANCE_COUNT,
                    instance_type=Config.SAGEMAKER_INSTANCE_TYPE,
                    model_name=model.attr_model_name,
                    variant_name="AllTraffic")])

        endpoint = sagemaker.CfnEndpoint(
            self, "RecommendationEndpoint",
            endpoint_name=Config.SAGEMAKER_ENDPOINT_NAME,
            endpoint_config_name=endpoint_config.attr_endpoint_config_name)

        for tag_key, tag_value in Config.TAGS.items():
            Tags.of(self).add(tag_key, tag_value)

        CfnOutput(self, "ModelName", value=model.model_name)
        CfnOutput(self, "EndpointName", value=endpoint.endpoint_name)
        CfnOutput(self, "RepositoryUri", value=repository.repository_uri)
