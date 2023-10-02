FROM eclipse-temurin:11 as appointment_ms
WORKDIR /app
COPY ./target/scala-3.3.0/quickstart-assembly-0.0.1-SNAPSHOT.jar .
CMD java -cp ./quickstart-assembly-0.0.1-SNAPSHOT.jar com.medisync.quickstart.Main
