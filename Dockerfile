FROM eclipse-temurin:21-jre-jammy
RUN useradd -s /bin/bash user
USER user
COPY --chown=644 /target/dina-user-api-*.jar /dina-user-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/dina-user-api.jar"]
