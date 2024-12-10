FROM python:3.9-slim

# Install dependencies
RUN pip install numpy pandas scikit-learn joblib boto3

# Create directories
RUN mkdir -p /opt/ml/code

# Copy training and inference scripts
COPY train.py /opt/ml/code/
COPY inference.py /opt/ml/code/

# Make scripts executable
RUN chmod +x /opt/ml/code/train.py
RUN chmod +x /opt/ml/code/inference.py

# Set working directory
WORKDIR /opt/ml/code

# Set the entrypoint
ENTRYPOINT ["python", "/opt/ml/code/train.py"]
