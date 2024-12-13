#!/usr/bin/env python3
import os
import json
import pickle
import pandas as pd
import numpy as np
from train import prepare_features  # Reuse preprocessing from train.py

def model_fn(model_dir):
    """Load model from the model_dir."""
    try:
        model = pickle.load(open(os.path.join(model_dir, 'model.pkl'), 'rb'))
        encoders = pickle.load(open(os.path.join(model_dir, 'encoders.pkl'), 'rb'))
        return {'model': model, 'encoders': encoders}
    except Exception as e:
        raise Exception(f"Error loading model: {str(e)}")

def input_fn(request_body, request_content_type):
    """Parse input data."""
    if request_content_type == 'application/json':
        data = json.loads(request_body)
        return pd.DataFrame.from_dict(data)
    raise ValueError(f"Unsupported content type: {request_content_type}")

def predict_fn(input_data, model_dict):
    """Make prediction using model."""
    try:
        model = model_dict['model']
        encoders = model_dict['encoders']

        categorical_columns = ['age_group', 'gender', 'location', 'preferred_categories']
        numerical_columns = ['avg_order_value', 'total_orders']

        X_processed, _ = prepare_features(input_data, categorical_columns, numerical_columns)
        prediction = model.predict(X_processed)

        # Convert days prediction back to dates
        reference_date = encoders['reference_date']
        predicted_dates = pd.Timestamp(reference_date) + pd.Timedelta(days=float(prediction))

        return predicted_dates.strftime('%Y-%m-%d').tolist()
    except Exception as e:
        raise Exception(f"Error during prediction: {str(e)}")

def output_fn(prediction, accept):
    """Format prediction response."""
    if accept == 'application/json':
        return json.dumps(prediction)
    raise ValueError(f"Unsupported accept type: {accept}")
