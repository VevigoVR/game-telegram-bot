package com.creazione.space_learning.queries;

import com.creazione.space_learning.exception.CommandConflictException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CommandRegistry implements ApplicationContextAware {
    private final List<Query> commandList = new ArrayList<>();
    private final Map<String, List<String>> commandToClasses = new HashMap<>();
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        registerAnnotatedCommands();
        validateCommandUniqueness();
        logRegisteredCommands();
    }

    private void registerAnnotatedCommands() {
        Map<String, Object> beans = context.getBeansWithAnnotation(GameCommand.class);

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            String beanName = entry.getKey();

            if (bean instanceof Query) {
                Query query = (Query) bean;
                GameCommand annotation = query.getClass().getAnnotation(GameCommand.class);

                // Регистрируем команды для проверки уникальности
                for (String command : annotation.value()) {
                    String normalizedCommand = command.toLowerCase().trim();
                    commandToClasses.computeIfAbsent(normalizedCommand, k -> new ArrayList<>())
                            .add(query.getClass().getName());
                }

                query.setQueries(List.of(annotation.value()));
                commandList.add(query);
            }
        }
    }

    private void validateCommandUniqueness() {
        Map<String, List<String>> duplicates = new LinkedHashMap<>();
        StringBuilder errorMessage = new StringBuilder();

        // Собираем дубликаты
        commandToClasses.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> duplicates.put(entry.getKey(), entry.getValue()));

        if (!duplicates.isEmpty()) {
            errorMessage.append("\n\n").append("🔥 Обнаружены конфликтующие команды:").append("\n");

            duplicates.forEach((cmd, classes) -> {
                errorMessage.append("\n▸ Команда: '").append(cmd).append("'\n");
                errorMessage.append("  Обработчики:\n");
                classes.forEach(cls -> errorMessage.append("    • ").append(cls).append("\n"));
            });

            errorMessage.append("\n💡 Решение: Убедитесь, что каждая команда имеет уникальный обработчик");
            throw new CommandConflictException(errorMessage.toString());
        }
    }

    private void validateCommandUniqueness2() {
        List<String> duplicateCommands = new ArrayList<>();
        StringBuilder errorMessage = new StringBuilder("\n\n⚠️ Обнаружены дублирующиеся команды:\n");

        // Находим команды с несколькими обработчиками
        for (Map.Entry<String, List<String>> entry : commandToClasses.entrySet()) {
            if (entry.getValue().size() > 1) {
                String command = entry.getKey();
                String classes = String.join(", ", entry.getValue());
                duplicateCommands.add(command);

                errorMessage.append("\n• Команда: '")
                        .append(command)
                        .append("'\n  Обработчики: ")
                        .append(classes);
            }
        }

        if (!duplicateCommands.isEmpty()) {
            errorMessage.append("\n\n❌ Приложение остановлено из-за конфликта команд.");
            throw new CommandConflictException(errorMessage.toString());
        }
    }

    private void logRegisteredCommands() {
        if (commandToClasses.isEmpty()) {
            System.out.println("ℹ️ Не зарегистрировано ни одной команды");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ УСПЕШНО ЗАРЕГИСТРИРОВАННЫЕ КОМАНДЫ");
        System.out.println("=".repeat(60));

        commandToClasses.forEach((cmd, classes) ->
                System.out.printf("▸ %-20s → %s%n", cmd, classes.get(0))
        );

        System.out.printf("%nВсего команд: %d, обработчиков: %d%n",
                commandToClasses.size(), commandList.size());
        System.out.println("=".repeat(60));
    }

    private void logRegisteredCommands2() {
        if (commandToClasses.isEmpty()) {
            System.out.println("ℹ️ Не зарегистрировано ни одной команды");
            return;
        }

        System.out.println("\n✅ Зарегистрированные команды:");
        commandToClasses.forEach((command, classes) ->
                System.out.printf("• %-15s → %s%n", command, classes.get(0))
        );

        System.out.printf("\nВсего команд: %d, обработчиков: %d%n",
                commandToClasses.size(), commandList.size());
    }

    public List<Query> getCommandList() {
        return commandList;
    }
}