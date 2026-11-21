# Build context: repository root
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY warehouse-service/pom.xml warehouse-service/pom.xml
COPY central-monitoring-service/pom.xml central-monitoring-service/pom.xml
RUN mvn -B -q -pl warehouse-service -am dependency:go-offline
COPY warehouse-service/src warehouse-service/src
RUN mvn -B -q -pl warehouse-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home sensors
COPY --from=build /app/warehouse-service/target/lib ./lib
COPY --from=build /app/warehouse-service/target/warehouse-service.jar ./app.jar
USER sensors
EXPOSE 3344/udp 3355/udp
ENTRYPOINT ["java", "-jar", "app.jar"]
