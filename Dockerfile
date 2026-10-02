# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:21-jre
WORKDIR /data
COPY --from=build /build/target/*.jar /app/app.jar
EXPOSE 8080
EXPOSE 6061
VOLUME /data
ENTRYPOINT ["java", "-jar", "/app/app.jar"]