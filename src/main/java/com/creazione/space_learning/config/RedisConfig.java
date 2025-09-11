package com.creazione.space_learning.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Настройка валидатора для полиморфной десериализации
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.creazione.space_learning.") // Разрешаем все подпакеты
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .allowIfSubType("org.hibernate.collection.spi.") // Разрешаем Hibernate коллекции
                .allowIfSubType("org.hibernate.proxy.pojo.bytebuddy.") // Разрешаем Hibernate прокси
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Сериализатор для ключей (всегда String)
        template.setKeySerializer(new StringRedisSerializer());

        // Сериализатор для обычных значений
        template.setValueSerializer(serializer);

        // Сериализатор для hash-ключей
        template.setHashKeySerializer(new StringRedisSerializer());

        // Сериализатор для hash-значений
        template.setHashValueSerializer(serializer);

        // Установка сериализатора для всех типов операций
        template.setDefaultSerializer(serializer);

        // Явное указание сериализаторов для конкретных типов операций
        template.setStringSerializer(new StringRedisSerializer());

        return template;
    }

    // Дополнительные бины для удобства работы с разными типами операций
    @Bean
    public RedisSerializer<Object> redisSerializer() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.creazione.space_learning.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}