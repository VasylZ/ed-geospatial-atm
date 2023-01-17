#FROM openjdk:11
FROM openjdk:11-jre-slim
MAINTAINER vasyl.zarva@gmail.com

ENV ENV_PROFILE="prod"
ENV APP_HOME=/geospatial-atm

RUN mkdir $APP_HOME

COPY target/ed-processing-1.0-SNAPSHOT-shaded.jar $APP_HOME/processing.jar
ENTRYPOINT ["java","-jar","/processing.jar"]

WORKDIR $APP_HOME
ENTRYPOINT ["java","-jar","processing.jar"]