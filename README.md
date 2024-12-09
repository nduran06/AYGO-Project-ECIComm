# AYGO-Project-ECIComm

# Initialize a new CDK project

export JSII_SILENCE_WARNING_UNTESTED_NODE_VERSION=1

mkdir sagemaker_cdk && cd sagemaker_cdk
cdk init app --language python

mkdir source && touch __init__.py


# Install dependencies

sudo apt-get install -y podman

# Create virtual environment
python3 -m venv .venv

# Or activate it (Mac/Linux)
source .venv/bin/activate

# Then install the dependencies

npm install -g aws-cdk

pip3 install aws-cdk-lib constructs boto3 sagemaker pandas scikit-learn

# Create the model ()/resources...

## create_push_model.sh

# Deploy the stack

cdk bootstrap --show-template > bootstrap-template.yaml
cdk bootstrap --template bootstrap-template.yaml
cdk list
cdk synth
cdk deploy -r arn:aws:iam::645349541441:role/LabRole


chmod +x build_push.sh
./build_push.sh





# To destroy when done
cdk destroy
