FROM sbtscala/scala-sbt:eclipse-temurin-focal-17.0.9_9_1.9.7_3.3.1 AS build
RUN mkdir -p /root/project-build/project
WORKDIR /root/project-build

# Download dependencies
ADD ./project/plugins.sbt project/
ADD ./project/build.properties project/
ADD build.sbt .
RUN sbt update

# Add and compile our actual application source code
ADD ./src /root/project-build/src
RUN sbt clean assembly

# Copy the jar to a consistent location
RUN cp ./target/scala-3.3.0/appointments.jar ./appointments.jar


FROM eclipse-temurin:17-alpine AS appointment_ms
WORKDIR /app
COPY --from=build ./root/project-build/appointments.jar .
CMD [ "java", "-jar", "appointments.jar" ]