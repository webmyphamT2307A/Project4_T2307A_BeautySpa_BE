# Stage 1: Build with Maven + Java 21
FROM maven:3.9.3-eclipse-temurin-21 AS build
WORKDIR /app

# Copy source code
COPY . .

# Optional: cache dependencies
RUN chmod +x ./mvnw && ./mvnw -B -DskipTests clean package

# Stage 2: Run Spring Boot with JDK 21
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
