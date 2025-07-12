echo -n $API_KEY | gcloud secrets create API_KEY --data-file=-
echo -n $LLM_URL | gcloud secrets create LLM_URL --data-file=-