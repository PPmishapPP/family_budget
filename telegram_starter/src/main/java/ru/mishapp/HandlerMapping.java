package ru.mishapp;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class HandlerMapping {

    private final Map<String, MethodMapping> methodMappingMap;

    public Optional<String> execute(String text) {
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
            return Optional
                    .ofNullable(methodMappingMap.get(entity))
                    .map(m -> m.execute(command));
        }
        return Optional.empty();
    }
}
