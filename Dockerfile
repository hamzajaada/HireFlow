# Une seule image pour les 6 applications Spring Boot : le module est passé en argument.
# Avant : ./mvnw clean package -DskipTests   (crée <module>/target/<module>-0.0.1-SNAPSHOT.jar)
FROM eclipse-temurin:17-jre
ARG MODULE
WORKDIR /app
COPY ${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
