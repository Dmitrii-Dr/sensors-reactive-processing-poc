# Build context: repository root
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY warehouse-service/pom.xml warehouse-service/pom.xml
COPY central-monitoring-service/pom.xml central-monitoring-service/pom.xml
RUN mvn -B -q -pl central-monitoring-service -am dependency:go-offline
COPY central-monitoring-service/src central-monitoring-service/src
RUN mvn -B -q -pl central-monitoring-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home monitoring
COPY --from=build /app/central-monitoring-service/target/lib ./lib
COPY --from=build /app/central-monitoring-service/target/central-monitoring-service.jar ./app.jar
USER monitoring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
