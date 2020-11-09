# Run stage
FROM openjdk:14-alpine
WORKDIR /app

# copy target/find-links.jar /usr/local/runme/app.jar
COPY build/libs/evidence.jar evidence.jar

ENTRYPOINT exec java $JAVA_OPTS -jar evidence.jar $EVIDENCE_OPTS
