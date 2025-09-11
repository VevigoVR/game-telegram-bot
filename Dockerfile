FROM amazoncorretto:21

EXPOSE 8080

VOLUME /tmp

# Копируем JVM настройки
COPY jvm.config /jvm.config

COPY build/libs/*.jar app.jar

# Используем оптимизированные JVM настройки
ENTRYPOINT ["java", "@/jvm.config", "-jar", "/app.jar"]