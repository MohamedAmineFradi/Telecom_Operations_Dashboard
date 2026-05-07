FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY call-service/target/*.jar app.jar
EXPOSE 8087
CMD ["java", "-jar", "app.jar"]
