#FROM openjdk:11
FROM openjdk:11-jre-slim
MAINTAINER vasyl.zarva@gmail.com

ENV APP_HOME=/geospatial-atm

RUN mkdir $APP_HOME

COPY target/ed-reader-web-1.0-SNAPSHOT-shaded.jar $APP_HOME/reader-web.jar

WORKDIR $APP_HOME
EXPOSE 8080
ENTRYPOINT ["java","-jar","reader-web.jar"]