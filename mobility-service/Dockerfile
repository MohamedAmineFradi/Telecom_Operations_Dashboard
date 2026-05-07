FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache libstdc++
WORKDIR /app
COPY mobility-service/target/*.jar app.jar
EXPOSE 8085
CMD ["java", "-jar", "app.jar"]
