# Stage 1: Build dự án bằng image Maven chính thức
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Chỉ copy file pom.xml trước để tận dụng cache của Docker cho các dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy mã nguồn và tiến hành build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime (Chỉ chứa JRE để nhẹ nhất)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file jar từ stage build
COPY --from=build /app/target/*.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]