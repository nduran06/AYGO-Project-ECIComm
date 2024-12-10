#!/bin/bash
if [ -f "/tmp/model.pkl" ] && [ -f "/tmp/encoders.pkl" ]; then
    mkdir -p /opt/ml/model
    cp /tmp/model.pkl /opt/ml/model/
    cp /tmp/encoders.pkl /opt/ml/model/
fi
