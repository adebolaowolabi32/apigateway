FROM openjdk:11-jre-slim

LABEL maintainer="laura.okpara@interswitchgroup.com"

EXPOSE 9090

ENV http_proxy 'http://172.16.10.20:8080/'
ENV https_proxy 'http://172.16.10.20:8080/'

ADD target/${project.build.finalName}.jar /opt/${project.artifactId}

WORKDIR /opt/${project.artifactId}

CMD ["java", "-jar", "${project.build.finalName}.jar"]





