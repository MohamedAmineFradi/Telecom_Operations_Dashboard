# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
ARG CACHEBUST=1
RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw && ./mvnw clean package -pl internet-service -am -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY --from=build /app/internet-service/target/internet-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8088
CMD ["java", "-jar", "app.jar"]
