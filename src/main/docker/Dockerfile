FROM delitescere/java:8
MAINTAINER Chris Phillipson <fastnsilver@gmail.com>
VOLUME /tmp
ADD @project.artifactId@-@project.version@.jar app.jar
ENTRYPOINT ["java","-jar","-Djava.security.egd=file:/dev/./urandom","/app.jar"]