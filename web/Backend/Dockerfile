# Bước 1: Build file JAR từ mã nguồn Java Spring Boot (Sử dụng Eclipse Temurin 17)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Bước 2: Khởi chạy file JAR bằng môi trường Java Runtime (Sử dụng Eclipse Temurin 17 JRE)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/jenika-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
