# Contexte de build = racine du repo GatchaApi (voir docker-compose.yaml), pour pouvoir
# construire et installer la lib partagée gatcha-common-security avant ce service.
FROM maven:3.9.6-amazoncorretto-21 AS common-build
WORKDIR /common
COPY gatcha-common-security/pom.xml .
COPY gatcha-common-security/src ./src
RUN mvn -q install -DskipTests

# --- Build Stage ---
FROM maven:3.9.6-amazoncorretto-21 AS build
COPY --from=common-build /root/.m2 /root/.m2
WORKDIR /app
COPY API_joueur/pom.xml .
COPY API_joueur/src ./src
RUN mvn clean package -DskipTests

# --- Run Stage ---
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
