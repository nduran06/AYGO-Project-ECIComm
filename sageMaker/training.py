import boto3
import sagemaker
from sagemaker.sklearn.estimator import SKLearn
import pandas as pd
from io import StringIO

# Assuming you have uploaded the generated data to S3
s3 = boto3.client('s3')
data_bucket = 'ecicommsagemakerbucket'
user_profiles_key = 'user_profiles.csv'
products_key = 'products.csv'
user_behavior_key = 'user_behavior.csv'
orders_key = 'orders.csv'
recommendations_key = 'product_recommendations.csv'

# Download the data from S3
user_profiles_obj = s3.get_object(Bucket=data_bucket, Key=user_profiles_key)
user_profiles_data = pd.read_csv(StringIO(user_profiles_obj['Body'].read().decode('utf-8')))

products_obj = s3.get_object(Bucket=data_bucket, Key=products_key)
products_data = pd.read_csv(StringIO(products_obj['Body'].read().decode('utf-8')))

user_behavior_obj = s3.get_object(Bucket=data_bucket, Key=user_behavior_key)
user_behavior_data = pd.read_csv(StringIO(user_behavior_obj['Body'].read().decode('utf-8')))

orders_obj = s3.get_object(Bucket=data_bucket, Key=orders_key)
orders_data = pd.read_csv(StringIO(orders_obj['Body'].read().decode('utf-8')))

recommendations_obj = s3.get_object(Bucket=data_bucket, Key=recommendations_key)
recommendations_data = pd.read_csv(StringIO(recommendations_obj['Body'].read().decode('utf-8')))

# Prepare the data for training
# (This part will depend on your specific modeling requirements)
X_train = user_profiles_data[['age_group', 'gender', 'location', 'preferred_categories', 'avg_order_value', 'total_orders']]
y_train = user_profiles_data['last_purchase_date']

# Define the model and hyperparameters
estimator = SKLearn(entry_point='train.py',
                   source_dir='./source',
                   instance_type='ml.m5.large',
                   instance_count=1,
                   framework_version='0.23-1',
                   role=sagemaker.get_execution_role())

# Train the model
estimator.fit({'training': f's3://{data_bucket}/data'})

# Deploy the model
predictor = estimator.deploy(initial_instance_count=1, instance_type='ml.m5.large')

# Test the model
test_data = user_profiles_data.iloc[-100:]
predictions = predictor.predict(test_data)
