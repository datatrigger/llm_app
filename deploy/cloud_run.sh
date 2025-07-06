# Firestore permission
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/datastore.user"

# Cloud run
gcloud run deploy llm-backend \
  --image=europe-west6-docker.pkg.dev/llm-server-461514/docker-repo/backend:latest \
  --platform=managed \
  --region=europe-west6 \
  --allow-unauthenticated \
  --port=8080 \
  --set-env-vars="API_KEY=$API_KEY,LLM_URL=$LLM_URL" \
  --memory=1Gi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=1