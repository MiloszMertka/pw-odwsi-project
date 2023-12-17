FROM eclipse-temurin:21
COPY target/security-0.0.1-SNAPSHOT.jar security-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-Dspring.profiles.active=production","-jar","/security-0.0.1-SNAPSHOT.jar"]
