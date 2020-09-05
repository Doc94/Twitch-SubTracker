#
# Build stage
#
FROM maven:3.6.3-jdk-11 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY entrypoint.sh /home/app
RUN mvn -f /home/app/pom.xml dependency:go-offline -B
RUN mvn -f /home/app/pom.xml package

#
# Package stage
#
FROM openjdk:11.0.6-jdk AS runtime
COPY --from=build /home/app/target/app.jar /usr/local/app.jar
COPY --from=build /home/app/entrypoint.sh /usr/local/entrypoint.sh

CMD ["/bin/bash", "/usr/local/entrypoint.sh"]