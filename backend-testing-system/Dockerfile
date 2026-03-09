FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY app/pom.xml app/
COPY core-testing-system/pom.xml core-testing-system/
COPY security/pom.xml security/

RUN mvn dependency:go-offline

COPY core-testing-system/src core-testing-system/src
COPY security/src security/src
COPY app/src app/src

RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/app/target/app-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]