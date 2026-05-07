FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY producer/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]