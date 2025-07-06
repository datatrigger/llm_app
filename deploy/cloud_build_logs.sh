gsutil mb -p $PROJECT_ID -l europe-west6 gs://llm-server-cloud-build-logs/

gcloud storage buckets add-iam-policy-binding gs://llm-server-cloud-build-logs \
    --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
    --role="roles/storage.objectCreator"

    gcloud storage buckets add-iam-policy-binding gs://llm-server-cloud-build-logs \
    --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
    --role="roles/storage.objectViewer"