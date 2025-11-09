FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/peaks-hikes-service-1.0.0.jar ./peaks-hikes-service.jar
EXPOSE 8082
CMD ["java", "-jar", "peaks-hikes-service.jar"]