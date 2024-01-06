FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "/app/target/security-0.0.1-SNAPSHOT.jar"]
