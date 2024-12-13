FROM python:3.8-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    python3-dev \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /opt/ml/code

# Set permissions for SageMaker directories
RUN mkdir -p /opt/ml/model \
    && mkdir -p /opt/ml/output \
    && mkdir -p /opt/ml/input/data \
    && chmod -R 777 /opt/ml

# Copy requirements file
COPY requirements.txt .

# Install Python dependencies
RUN pip install -r requirements.txt

# Copy training code
COPY train.py .
COPY inference.py .

# Make train.py executable
RUN chmod +x train.py

ENTRYPOINT ["python", "train.py"]
