# Une seule image pour les 7 applications Spring Boot : le module est passé en argument.
# Avant : ./mvnw clean package -DskipTests   (crée <module>/target/<module>-0.0.1-SNAPSHOT.jar)
FROM eclipse-temurin:17-jre
ARG MODULE
WORKDIR /app
COPY ${MODULE}/target/*.jar app.jar
# Mémoire limitée par application : les 7 JVM doivent tenir ensemble sur un PC portable.
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
ENTRYPOINT ["java", "-jar", "app.jar"]
