FROM eclipse-temurin:17-jdk-alpine
WOKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT["java","-jar","app.jar"]