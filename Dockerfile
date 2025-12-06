# 1. Stage: Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /LeihSy_Backend


# Maven installieren
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Maven Dependencies cachen
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Projekt kopieren und bauen
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Stage: Runtime
FROM eclipse-temurin:21-jdk
WORKDIR /LeihSy_Backend

# Jar aus der Build-Stage kopieren
COPY --from=build /LeihSy_Backend/target/*.jar app.jar

# Port setzen (Spring Boot default: 8080)
EXPOSE 8080

# Environment Variables (optional, z.B. für DB)
ENV POSTGRES_URL=jdbc:postgresql://db:5432/leihsy
ENV POSTGRES_USER=leihsyuser
ENV POSTGRES_PASSWORD=secret

# Startbefehl
ENTRYPOINT ["java","-jar","app.jar"]
