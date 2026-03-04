FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_NAME=traffic-service.jar
COPY target/${JAR_NAME} app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
