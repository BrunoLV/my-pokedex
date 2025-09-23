FROM openjdk:21-jdk AS builder
WORKDIR /project

# copy everything (including gradle wrapper) and build the fat jar
COPY . .
RUN chmod +x ./gradlew \
    && ./gradlew clean shadowJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

# copy the fat jar produced by the builder stage
COPY --from=builder /project/build/libs/*all*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
