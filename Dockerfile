FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/server/target/*-jar-with-dependencies.jar server.jar
ENTRYPOINT ["java", "-jar", "server.jar"]
