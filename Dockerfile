FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM tomcat:10.1.24-jdk17-temurin

ENV CATALINA_OPTS="-Djava.awt.headless=true"

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/todo-app.war /usr/local/tomcat/webapps/todo-app.war

EXPOSE 8080