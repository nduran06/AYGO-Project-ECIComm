import os
import json
import pickle
import sys
import traceback
import boto3
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
from io import BytesIO

# SageMaker paths
prefix = '/opt/ml/'
input_path = prefix + 'input/data'
output_path = os.path.join(prefix, 'output')
model_path = os.path.join(prefix, 'model')
param_path = os.path.join(prefix, 'input/config/hyperparameters.json')

def load_data_from_s3(bucket, key):
    s3 = boto3.client('s3')
    obj = s3.get_object(Bucket=bucket, Key=key)
    data = pd.read_csv(BytesIO(obj['Body'].read()))
    return data

def get_hyperparameters():
    """
    Retrieves hyperparameters with graceful fallback to defaults.
    Returns a dictionary of hyperparameters.
    """
    default_hyperparameters = {
        'n_estimators': 100,
        'max_depth': 10
    }

    try:
        print(f"Attempting to read hyperparameters from: {param_path}")

        # First, verify if the directory exists
        config_dir = os.path.dirname(param_path)
        if os.path.exists(config_dir):
            print(f"Config directory exists at: {config_dir}")
            print(f"Contents of config directory: {os.listdir(config_dir)}")
        else:
            print(f"Config directory does not exist at: {config_dir}")
            return default_hyperparameters

        # Try to read the hyperparameters file
        if os.path.exists(param_path):
            print("Hyperparameters file found, reading contents...")
            with open(param_path, 'r') as tc:
                loaded_hyperparameters = json.load(tc)
                print(f"Loaded hyperparameters: {loaded_hyperparameters}")
                return loaded_hyperparameters
        else:
            print(f"No hyperparameters file found at {param_path}")
            return default_hyperparameters

    except Exception as e:
        print(f"Error reading hyperparameters: {str(e)}")
        print("Falling back to default hyperparameters")
        return default_hyperparameters

def clean_numerical_column(series, column_name):
    """
    Clean a numerical column by handling invalid values appropriately.
    Returns the cleaned series and information about what was cleaned.
    """
    print(f"\nCleaning column: {column_name}")
    print(f"Initial unique values sample: {series.unique()[:5]}")

    # First, convert to numeric while logging any conversion failures
    cleaned = pd.to_numeric(series, errors='coerce')

    # Check for and handle NaN values
    nan_count = cleaned.isna().sum()
    if nan_count > 0:
        print(f"Found {nan_count} NaN values in {column_name}")
        # Calculate median of non-NaN values
        valid_median = cleaned.dropna().median()
        if pd.isna(valid_median):
            # If all values are NaN, use 0 as a fallback
            print(f"Warning: All values in {column_name} are NaN, using 0 as fallback")
            valid_median = 0
        cleaned = cleaned.fillna(valid_median)

    # Handle infinite values
    inf_mask = np.isinf(cleaned)
    inf_count = inf_mask.sum()
    if inf_count > 0:
        print(f"Found {inf_count} infinite values in {column_name}")
        # Replace infinite values with the maximum non-infinite value
        non_inf_max = cleaned[~inf_mask].max()
        cleaned[inf_mask] = non_inf_max if not pd.isna(non_inf_max) else 0

    # Ensure values are within a reasonable range
    if not cleaned.empty:
        # Calculate boundaries using percentiles of non-NaN, non-infinite values
        valid_data = cleaned[~np.isinf(cleaned)].dropna()
        if not valid_data.empty:
            lower_bound = valid_data.quantile(0.01)
            upper_bound = valid_data.quantile(0.99)
            cleaned = cleaned.clip(lower=lower_bound, upper=upper_bound)

    # Convert to float32 and handle any remaining issues
    cleaned = cleaned.astype('float32')

    print(f"Final value range: [{cleaned.min()}, {cleaned.max()}]")
    return cleaned

def prepare_features(df, categorical_columns, numerical_columns):
    """
    Prepare all features with comprehensive error checking and logging.
    """
    print("Starting feature preparation...")
    X_processed = pd.DataFrame()
    encoders = {}

    # Process categorical columns
    for col in categorical_columns:
        print(f"\nProcessing categorical column: {col}")
        # Convert to string and handle empty values
        series = df[col].astype(str)
        series = series.replace('', 'unknown')
        series = series.fillna('unknown')

        # Fit encoder and transform
        encoder = LabelEncoder()
        encoded_values = encoder.fit_transform(series)
        X_processed[col] = encoded_values.astype('float32')
        encoders[col] = encoder

        print(f"Unique values count: {len(encoder.classes_)}")

    # Process numerical columns
    for col in numerical_columns:
        X_processed[col] = clean_numerical_column(df[col], col)

    # Final verification
    print("\nFeature preparation complete:")
    print(f"Shape: {X_processed.shape}")
    print("Data types:")
    print(X_processed.dtypes)
    print("Any NaN values:", X_processed.isna().any().any())
    print("Value ranges:")
    for col in X_processed.columns:
        print(f"{col}: [{X_processed[col].min()}, {X_processed[col].max()}]")

    return X_processed, encoders

def prepare_target(dates):
    """
    Prepare the target variable (dates) with comprehensive validation.
    """
    print("\nPreparing target variable...")

    # Convert to datetime and handle invalid dates
    clean_dates = pd.to_datetime(dates, errors='coerce')

    # Handle any NaN dates
    if clean_dates.isna().any():
        print("Warning: Found invalid dates, using earliest valid date as replacement")
        valid_dates = clean_dates.dropna()
        if valid_dates.empty:
            raise ValueError("No valid dates found in target variable")
        replacement_date = valid_dates.min()
        clean_dates = clean_dates.fillna(replacement_date)

    # Convert to days since earliest date
    reference_date = clean_dates.min()
    days_since_ref = (clean_dates - reference_date).dt.total_seconds() / (24 * 60 * 60)

    # Convert to float32 and verify
    y = days_since_ref.astype('float32')

    print(f"Target variable statistics:")
    print(f"Reference date: {reference_date}")
    print(f"Range: [{y.min()}, {y.max()}] days")
    print(f"Data type: {y.dtype}")

    return y, reference_date

def save_model_artifacts(model, encoders, model_dir):
    """
    Save model artifacts using a temporary directory approach to handle
    SageMaker's file system permissions.

    Args:
        model: The trained model object
        encoders: Dictionary of encoders used for feature transformation
        model_dir: Target directory for model artifacts
    """
    print(f"Starting model artifact saving process...")

    # Create a temporary directory in /tmp which is always writable
    temp_dir = '/tmp/model_artifacts'
    os.makedirs(temp_dir, exist_ok=True)
    print(f"Created temporary directory: {temp_dir}")

    try:
        # First save files to temporary location
        temp_model_path = os.path.join(temp_dir, 'model.pkl')
        temp_encoders_path = os.path.join(temp_dir, 'encoders.pkl')

        print("Saving model to temporary location...")
        with open(temp_model_path, 'wb') as f:
            pickle.dump(model, f)

        print("Saving encoders to temporary location...")
        with open(temp_encoders_path, 'wb') as f:
            pickle.dump(encoders, f)

        # Create model directory if it doesn't exist
        os.makedirs(model_dir, exist_ok=True)

        # Move files from temporary location to final destination
        print(f"Moving files to final destination: {model_dir}")
        shutil.move(temp_model_path, os.path.join(model_dir, 'model.pkl'))
        shutil.move(temp_encoders_path, os.path.join(model_dir, 'encoders.pkl'))

        print("Successfully saved model artifacts")

    except Exception as e:
        print(f"Error during model saving: {str(e)}")
        print("\nDebug information:")
        print(f"Temporary directory exists: {os.path.exists(temp_dir)}")
        print(f"Model directory exists: {os.path.exists(model_dir)}")
        print("\nDirectory permissions:")
        os.system(f"ls -la {temp_dir}")
        os.system(f"ls -la {model_dir}")
        raise
    finally:
        # Clean up temporary directory
        if os.path.exists(temp_dir):
            shutil.rmtree(temp_dir)
            print("Cleaned up temporary directory")

def train():
    try:
        # Create the necessary directories
        if not os.path.exists(os.path.dirname(param_path)):
            os.makedirs(os.path.dirname(param_path))

        if not os.path.exists(output_path):
            os.makedirs(output_path)

        # Read hyperparameters
        print("Starting training process...")

        # Get hyperparameters with fallback to defaults
        hyperparameters = get_hyperparameters()

        # Convert hyperparameters from strings
        n_estimators = int(hyperparameters.get('n_estimators', 100))
        max_depth = int(hyperparameters.get('max_depth', 10))

        print(f"Using hyperparameters: n_estimators={n_estimators}, max_depth={max_depth}")

        # Load data from S3
        bucket = 'ecicommsagemakerbucket'
        data_files = {
            'user_profiles': 'training-data/user_profiles.csv',
            'products': 'training-data/products.csv',
            'user_behavior': 'training-data/user_behavior.csv',
            'orders': 'training-data/orders.csv',
            'recommendations': 'training-data/product_recommendations.csv'
        }
        data = {}
        for name, key in data_files.items():
            data[name] = load_data_from_s3(bucket, key)
        user_profiles_data = data['user_profiles']

        # Prepare features and target
        # Define column types
        categorical_columns = ['age_group', 'gender', 'location', 'preferred_categories']
        numerical_columns = ['avg_order_value', 'total_orders']

        # Prepare features
        X, encoders = prepare_features(user_profiles_data, categorical_columns, numerical_columns)

        # Prepare target
        y, reference_date = prepare_target(user_profiles_data['last_purchase_date'])
        encoders['reference_date'] = reference_date

        # Final validation before model training
        print("\nFinal validation before training:")
        print(f"X shape: {X.shape}")
        print(f"y shape: {y.shape}")
        print("Any NaN in X:", X.isna().any().any())
        print("Any NaN in y:", y.isna().any())

        # If we still have any issues, raise an error before training
        if X.isna().any().any() or y.isna().any():
            raise ValueError("Found NaN values after preprocessing")

        # Verify data before training
        print("\nFinal data verification:")
        print(f"X shape: {X.shape}")
        print(f"y shape: {y.shape}")
        print("X dtypes:\n", X.dtypes)
        print(f"y dtype: {y.dtype}")
        print("Any NaN in X:", X.isna().any().any())
        print("Any NaN in y:", y.isna().any())

        # Train model
        model = RandomForestRegressor(
            n_estimators=n_estimators,
            max_depth=max_depth,
            random_state=42
        )

        model.fit(X, y)

        # Save model and encoders
        print("\nTraining completed successfully. Saving model artifacts...")
        model_dir = '/opt/ml/model'

        # Verify the model directory exists and create if needed
        os.makedirs(model_dir, exist_ok=True)

        # Save the model artifacts
        save_model_artifacts(model, encoders, model_dir)

        print("Training job completed successfully")
        

    except Exception as e:
        print(f"Error details: {str(e)}")
        print("Data types in X:")
        print(X.dtypes)
        print("Data type of y:", type(y))
        trc = traceback.format_exc()
        print(trc)
        # Write failure information to the correct output path
        failure_file = os.path.join('/opt/ml/output', 'failure')
        os.makedirs(os.path.dirname(failure_file), exist_ok=True)
        with open(failure_file, 'w') as f:
            f.write(f'Exception during training: {str(e)}\n{trc}')

        # Exit with error code
        sys.exit(255)

if __name__ == '__main__':
    train()
    sys.exit(0)
