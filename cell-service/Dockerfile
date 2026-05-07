FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY cell-service/target/*.jar app.jar
EXPOSE 8084
CMD ["java", "-jar", "app.jar"]
