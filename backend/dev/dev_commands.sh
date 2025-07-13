# gcloud commands
gcloud init
gcloud auth application-default login

# Call LLM private service
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="user:$(gcloud config get-value account)" \
    --role="roles/run.invoker"
gcloud run services get-iam-policy llm-server --region=europe-west1

# gradle & docker
./gradlew clean build --refresh-dependencies
./gradlew bootBuildImage --imageName=europe-west6-docker.pkg.dev/$PROJECT_ID/docker-repo/backend:latest
gcloud auth configure-docker europe-west6-docker.pkg.dev
docker push europe-west6-docker.pkg.dev/$PROJECT_ID/docker-repo/backend:latest
