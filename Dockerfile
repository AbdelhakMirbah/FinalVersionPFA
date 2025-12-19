# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY .mvn ./.mvn
COPY mvnw .
RUN chmod +x mvnw
# Run build, skipping tests to be faster (tests were verified locally)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/fraud-detection-1.0.0.jar app.jar
# Required for ONNX model loading
COPY src/main/resources/fraud_model.onnx src/main/resources/fraud_model.onnx

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
