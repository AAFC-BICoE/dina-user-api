FROM openjdk:11-jre-slim
RUN useradd -s /bin/bash user
USER user
COPY --chown=644 /target/dina-user-api-*.jar /dina-user-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/dina-user-api.jar"]
