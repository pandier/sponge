FROM eclipse-temurin:21-alpine

EXPOSE 25565

WORKDIR /data
COPY vanilla/build/libs/*-universal.jar server.jar
RUN echo "eula=true" > eula.txt

ENTRYPOINT ["java", "-jar", "server.jar"]
