FROM eclipse-temurin:17-jre-alpine
# libstdc++ is required by the RocksDB JNI native library bundled inside
# kafka-streams.  Alpine uses musl libc and ships without libstdc++ by default;
# without it Kafka Streams throws UnsatisfiedLinkError at state-store init time.
RUN apk add --no-cache libstdc++
WORKDIR /app
ARG JAR_NAME=traffic-service.jar
COPY target/${JAR_NAME} app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
