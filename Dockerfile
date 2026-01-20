# --- Phase de construction (Build) ---
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copie du pom.xml et des sources
COPY pom.xml .
COPY src ./src

# Compilation (Le flag -X permet de voir les erreurs en détail si ça recrashe)
RUN mvn clean package -DskipTests

# --- Phase d'exécution (Run) ---
# On utilise aussi Corretto pour l'exécution (JRE optimisé)
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# Récupération du jar
COPY --from=build /app/target/*.jar app.jar

# Port
EXPOSE 8080

# Démarrage
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]