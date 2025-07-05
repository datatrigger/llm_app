# gcloud commands
gcloud init
gcloud auth application-default login

# gradle
./gradlew clean build --refresh-dependencies
./gradlew bootBuildImage --imageName=gcr.io/my-project/myapp:latest