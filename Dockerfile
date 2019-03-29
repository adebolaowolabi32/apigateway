FROM openjdk:11-jre-slim

LABEL maintainer="laura.okpara@interswitchgroup.com"

ADD target/api-management-gateway.jar /opt/api-management-gateway.jar

WORKDIR /opt

CMD ["java", "-jar", "api-management-gateway.jar"]



