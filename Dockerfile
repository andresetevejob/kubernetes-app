FROM eclipse-temurin:21
WORKDIR /opt
COPY target/kubernetes-app-0.0.2-SNAPSHOT.jar /opt/app.jar
ENV PORT 8080
EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar