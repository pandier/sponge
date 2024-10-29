FROM eclipse-temurin:21-alpine AS build

WORKDIR /build/
#COPY build.gradle.kts settings.gradle.kts gradlew /app/
#COPY gradle /app/gradle/
#RUN ./gradlew build || return 0
COPY . .
RUN ./gradlew clean assemble


FROM eclipse-temurin:21-alpine

EXPOSE 25565

WORKDIR /data
COPY --from=build /build/vanilla/build/libs/*-universal.jar server.jar
RUN echo "eula=true" > eula.txt

ENTRYPOINT ["java", "-jar", "server.jar"]
