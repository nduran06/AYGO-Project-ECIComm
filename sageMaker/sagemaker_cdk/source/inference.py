import os
import json
import pickle
import tarfile
import boto3
from io import BytesIO

def model_fn(model_dir):
    """
    Load model from tar.gz in SageMaker's model directory
    """
    print(f"Loading model from {model_dir}")

    # Path to tar.gz file
    model_path = os.path.join(model_dir, 'model.tar.gz')

    try:
        # Extract and load model from tar.gz
        with tarfile.open(model_path, 'r:gz') as tar:
            # Extract model
            model_member = tar.getmember('model.pkl')
            model_file = tar.extractfile(model_member)
            model = pickle.load(model_file)

            # Extract encoders
            encoders_member = tar.getmember('encoders.pkl')
            encoders_file = tar.extractfile(encoders_member)
            encoders = pickle.load(encoders_file)

        return {'model': model, 'encoders': encoders}
    except Exception as e:
        print(f"Error loading model: {str(e)}")
        raise

def input_fn(request_body, request_content_type):
    """Transform input to model format"""
    if request_content_type == 'application/json':
        input_data = json.loads(request_body)
        return input_data
    raise ValueError(f"Unsupported content type: {request_content_type}")

def predict_fn(input_data, model_dict):
    """Make prediction using loaded model"""
    try:
        model = model_dict['model']
        encoders = model_dict['encoders']
        # Your prediction logic here
        return prediction
    except Exception as e:
        print(f"Error during prediction: {str(e)}")
        raise

def output_fn(prediction, accept):
    """Transform prediction to response format"""
    if accept == 'application/json':
        response = json.dumps(prediction)
        return response
    raise ValueError(f"Unsupported accept type: {accept}")
