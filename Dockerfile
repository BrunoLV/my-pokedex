FROM openjdk:21-jdk-slim AS builder
WORKDIR /project

# install needed tools for the gradle wrapper (wget/curl/unzip, xargs via findutils) and ca-certificates
RUN apt-get update \
    && apt-get install -y --no-install-recommends bash wget unzip findutils ca-certificates curl git \
    && rm -rf /var/lib/apt/lists/*

# copy everything (including gradle wrapper) and build the fat jar
COPY . .
# ensure gradlew is unix-formatted and executable, then run the wrapper
RUN sed -i 's/\r$//' ./gradlew \
    && chmod +x ./gradlew \
    && ./gradlew clean shadowJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

# install curl (used by docker-compose healthcheck) and ca-certificates
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# copy the fat jar produced by the builder stage
COPY --from=builder /project/build/libs/*all*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
