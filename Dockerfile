FROM openjdk:11-jre-slim

LABEL maintainer="laura.okpara@interswitchgroup.com"

ADD target/api-gateway.jar /opt/api-gateway.jar

CMD ["java", "-jar", "/opt/api-gateway.jar"]



