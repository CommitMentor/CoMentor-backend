FROM amazoncorretto:17-alpine-jdk

ARG JAR_FILE=build/libs/*.jar
ARG PROFILES

ENV PROFILES=${PROFILES}

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$PROFILES -jar app.jar"]
