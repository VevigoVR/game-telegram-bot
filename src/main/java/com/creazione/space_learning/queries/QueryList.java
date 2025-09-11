package com.creazione.space_learning.queries;

import com.creazione.space_learning.queries.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QueryList {
    private final List<Query> queryList;
    private final CommandRegistry commandRegistry;

    @Autowired
    public QueryList(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
        this.queryList = createCombinedQueryList();
    }

    private List<Query> createCombinedQueryList() {
        List<Query> combined = new ArrayList<>();

        // Добавляем команды из аннотаций
        combined.addAll(commandRegistry.getCommandList());

        // Добавляем обработчики колбэков (без аннотаций)
        combined.addAll(getCallbackHandlers());

        return combined;
    }

    // Обработчики, которые не являются командами (колбэки)
    private List<Query> getCallbackHandlers() {
        return List.of(
                //new BuildingsQuery(),
                //new BuildingInfo(),
                //new UpBuilding()
        );
    }

    public List<Query> getQueryList() {
        return queryList;
    }
}