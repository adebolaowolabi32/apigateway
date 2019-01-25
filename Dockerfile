FROM openjdk:11-jre-slim

LABEL maintainer="laura.okpara@interswitchgroup.com"

EXPOSE 9090

ADD target/api-gateway-0.0.1-SNAPSHOT.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]





