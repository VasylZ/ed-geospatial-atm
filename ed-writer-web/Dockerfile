#FROM openjdk:11
FROM openjdk:11-jre-slim
MAINTAINER vasyl.zarva@gmail.com

ENV ENV_PROFILE="prod"
ENV APP_HOME=/geospatial-atm

RUN mkdir $APP_HOME

COPY target/ed-writer-web-1.0-SNAPSHOT-shaded.jar $APP_HOME/writer-web.jar
WORKDIR $APP_HOME
EXPOSE 8081
ENTRYPOINT ["java","-jar","writer-web.jar"]