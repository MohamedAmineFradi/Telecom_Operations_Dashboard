FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY alert-service/target/*.jar app.jar
EXPOSE 8083
CMD ["java", "-jar", "app.jar"]
