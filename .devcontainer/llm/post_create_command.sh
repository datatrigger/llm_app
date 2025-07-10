#!/bin/bash

echo "🚀 Starting LLM server development container setup..."
set -e  # Exit on any error (fail fast)

echo "☁️ Installing Google Cloud CLI..."
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
sudo apt-get update
sudo apt-get install -y google-cloud-cli

echo "✅ LLMserver development container setup complete!"