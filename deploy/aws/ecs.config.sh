#!/bin/bash

yum install -y aws-cli

# Replace <bucket-name> w/ your own S3 bucket below
aws s3 cp s3://<bucket-name>/ecs.config /etc/ecs.config
