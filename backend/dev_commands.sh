# gcloud commands
gcloud init
gcloud auth application-default login

# gradle
./gradlew clean build --refresh-dependencies
./gradlew bootBuildImage --imageName=gcr.io/my-project/myapp:latest

# docker
gcloud auth configure-docker europe-west6-docker.pkg.dev
./gradlew bootBuildImage --imageName=europe-west6-docker.pkg.dev/$PROJECT_ID/docker-repo/backend:latest
docker push europe-west6-docker.pkg.dev/$PROJECT_ID/docker-repo/backend:latest
