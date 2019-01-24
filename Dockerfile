FROM openjdk:11-jre-slim

LABEL maintainer "laura.okpara@interswitchgroup.com"

EXPOSE 9090

ENV http_proxy 'http://172.16.10.20:8080/'
ENV https_proxy 'http://172.16.10.20:8080/'

ADD target/api-gateway-0.0.1-SNAPSHOT.jar /opt/api-gateway

WORKDIR /opt/api-gateway

CMD ["java", "-jar", "api-gateway-0.0.1-SNAPSHOT.jar"]





