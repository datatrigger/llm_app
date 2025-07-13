#!/bin/bash
ENV=${1:-prod}
export GCLOUD_TOKEN=$(gcloud auth print-identity-token)
bru run --env $ENV --output results.json --verbose