FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 as appointments_ms
WORKDIR /app
COPY . .
CMD ["sbt run ."]
