
gcloud artifacts repositories create docker-repo \
    --repository-format=docker \
    --location=europe-west6 \
    --description="Docker repository for the llm server project"