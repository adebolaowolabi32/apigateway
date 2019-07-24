FROM openjdk:11-jre-slim

ENV TZ Africa/Lagos

LABEL maintainer="laura.okpara@interswitchgroup.com"

ADD target/api-gateway.jar /opt/api-gateway.jar

WORKDIR /opt

CMD ["java", "-jar", "api-gateway.jar"]



