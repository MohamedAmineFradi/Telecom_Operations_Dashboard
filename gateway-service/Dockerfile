FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY gateway-service/target/*.jar app.jar
EXPOSE 8090
CMD ["java", "-jar", "app.jar"]
