FROM openjdk:8-jdk-alpine
MAINTAINER Chris Phillipson <fastnsilver@gmail.com>
RUN mkdir -p /opt/@project.artifactId@/bin
COPY @project.artifactId@-@project.version@.jar /opt/@project.artifactId@/bin
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /opt/@project.artifactId@/bin/@project.artifactId@-@project.version@.jar