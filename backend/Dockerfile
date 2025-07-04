FROM openjdk:21-jdk-slim
WORKDIR /app

# Gradle
COPY gradlew .
COPY gradle gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN chmod +x gradlew

# Build
COPY src/main src/main
RUN ./gradlew build -x test --no-daemon
RUN cp build/libs/*.jar app.jar

# Run
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]