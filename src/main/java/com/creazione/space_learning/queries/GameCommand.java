package com.creazione.space_learning.queries;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameCommand {
    String[] value(); // Команды, которые обрабатывает этот класс
    String description() default "";
}