plugins {
	java
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.llmserver"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.google.cloud:spring-cloud-gcp-dependencies:6.2.2")
    }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("com.google.cloud:spring-cloud-gcp-starter-data-firestore")
	implementation("com.google.auth:google-auth-library-oauth2-http")
	implementation("net.logstash.logback:logstash-logback-encoder:8.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.wiremock.integrations:wiremock-spring-boot:3.10.0")
}

tasks.withType<Test> {
	useJUnitPlatform()

	testLogging {
		events("passed", "skipped", "failed")
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
	}
}
