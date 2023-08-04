package ru.mishapp;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class HandlerMapping {
    
    private final Map<String, IMethodMapping> methodMappingMap;
    
    public Optional<String> execute(String text, Long chatId) {
        text = text.trim();
        if (text.startsWith("/")) {
            int end = text.indexOf(" ");
            String entity;
            String command;
            if (end == -1) {
                entity = text.substring(1);
                command = "";
            } else {
                entity = text.substring(1, end);
                command = text.substring(entity.length() + 2);
            }
            IMethodMapping methodMapping = methodMappingMap.get(entity);
            if (methodMapping == null) {
                throw new IllegalArgumentException("Нет такого обработчика:" + entity);
            }
            return Optional.ofNullable(methodMapping.execute(command, chatId));
        }
        return Optional.empty();
    }
}
