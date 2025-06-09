FROM maven:3.8.5-openjdk-17-slim
WORKDIR /app
COPY target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]