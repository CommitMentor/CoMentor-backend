FROM amazoncorretto:17-alpine-jdk
ARG JAR_FILE=build/libs/*.jar
ARG PROFILES
ARG ENV
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]