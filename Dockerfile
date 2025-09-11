FROM amazoncorretto:21

EXPOSE 8083

VOLUME /tmp

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
