FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY internet-service/target/*.jar app.jar
EXPOSE 8088
CMD ["java", "-jar", "app.jar"]
