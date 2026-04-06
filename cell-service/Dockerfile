# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -pl cell-service -am -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY --from=build /app/cell-service/target/cell-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
CMD ["java", "-jar", "app.jar"]
