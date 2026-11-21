FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home sensors
COPY --from=build /app/target/lib ./lib
COPY --from=build /app/target/sensors-server.jar ./app.jar
USER sensors
EXPOSE 3344/udp 3355/udp
ENTRYPOINT ["java", "-jar", "app.jar"]
