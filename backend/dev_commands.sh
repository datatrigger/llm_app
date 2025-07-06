# gcloud commands
gcloud init
gcloud auth application-default login

# gradle & docker
./gradlew clean build --refresh-dependencies
./gradlew bootBuildImage --imageName=europe-west6-docker.pkg.dev/$PROJECT_ID/docker-repo/backend:latest
gcloud auth configure-docker europe-west6-docker.pkg.dev
docker push europe-west6-docker.pkg.dev/$PROJECT_ID/docker-repo/backend:latest
