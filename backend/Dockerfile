FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/gestion-rh-api-*.jar app.jar
COPY --from=build /app/src/main/resources/certs/aiven-minerva-ca.pem /app/certs/aiven-minerva-ca.pem
ENV AIVEN_SSL_CA=/app/certs/aiven-minerva-ca.pem \
    SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "app.jar"]
