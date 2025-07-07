# Deploy gemma 3 4b
# --region europe-west1 because no GPU available in europe-west6 in 2025

# Test (unsecured)
gcloud run deploy llm-server-test \
   --image us-docker.pkg.dev/cloudrun/container/gemma/gemma3-4b \
   --cpu 8 \
   --concurrency 4 \
   --set-env-vars OLLAMA_NUM_PARALLEL=4 \
   --set-env-vars API_KEY=$API_KEY \
   --gpu 1 \
   --gpu-type nvidia-l4 \
   --max-instances 1 \
   --memory 32Gi \
   --allow-unauthenticated \
   --no-cpu-throttling \
   --timeout=600 \
   --region europe-west1

# Example request
curl "$LLM_URL/v1beta/models/gemma-3-4b-it:generateContent?key=$API_KEY" \
   -H 'Content-Type: application/json' \
   -X POST \
   -d '{
     "contents": [{
       "parts":[{"text": "Hi, can you tell me a fun fact about Switzerland?"}]
       }]
      }'

# Prod
gcloud run deploy llm-server \
   --image us-docker.pkg.dev/cloudrun/container/gemma/gemma3-4b \
   --cpu 8 \
   --concurrency 4 \
   --set-env-vars OLLAMA_NUM_PARALLEL=4 \
   --set-secrets=API_KEY=API_KEY:latest \
   --gpu 1 \
   --gpu-type nvidia-l4 \
   --max-instances 1 \
   --memory 32Gi \
   --no-cpu-throttling \
   --timeout=600 \
   --region europe-west1

# Example request
curl "$LLM_URL/v1beta/models/gemma-3-4b-it:generateContent" \
  -H "Authorization: Bearer $(gcloud auth print-identity-token)" \
  -H 'Content-Type: application/json' \
  -X POST \
  -d '{
    "contents": [{
      "parts":[{"text": "Hi, can you tell me a fun fact about Switzerland?"}]
      }]
     }'