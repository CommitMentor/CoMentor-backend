FROM amazoncorretto:17-alpine-jdk

# Build arguments
ARG JAR_FILE=build/libs/*.jar
ARG FIREBASE_CREDENTIAL_BASE64
ARG PROFILES
ARG ENV

# Pass to ENV (for runtime use)
ENV FIREBASE_CREDENTIAL_BASE64=${FIREBASE_CREDENTIAL_BASE64}

# Copy jar
COPY ${JAR_FILE} app.jar

# Run Spring Boot with profile and env
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-DFIREBASE_CREDENTIAL_BASE64=${FIREBASE_CREDENTIAL_BASE64}", "-jar", "app.jar"]
