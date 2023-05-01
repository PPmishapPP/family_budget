package ru.mishapp;

import lombok.SneakyThrows;
import ru.mishapp.annotations.TelegramParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodMapping {
    
    private final Map<String, MethodAndParams> methodsByName = new HashMap<>();
    private final Object handler;
    
    public MethodMapping(Map<String, Method> methodMap, Object handler) {
        this.handler = handler;
        for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
            List<String> paramsNames = new ArrayList<>();
            for (Parameter parameter : entry.getValue().getParameters()) {
                TelegramParam annotation = parameter.getAnnotation(TelegramParam.class);
                if (annotation == null) {
                    if (parameter.getType() != Long.class) {
                        throw new IllegalStateException(
                            "Все параметры кроме 'chatId' должны быть помечены аннотацией '@TelegramParam'");
                    }
                } else {
                    paramsNames.add(annotation.value());
                }
            }
            methodsByName.put(entry.getKey(), new MethodAndParams(entry.getValue(), paramsNames));
        }
    }
    
    @SneakyThrows
    public String execute(String text, Long chatId) {
        int endIndex = text.indexOf(" ");
        String command = endIndex == -1 ? text : text.substring(0, endIndex);
        MethodAndParams methodAndParams = methodsByName.get(command);
        
        if (methodAndParams == null) {
            throw new IllegalArgumentException("Не найдено команды для этой сущности");
        }
        
        if (endIndex == -1) {
            return (String) methodAndParams.method.invoke(handler, chatId);
        }
        
        String arguments = text.substring(endIndex + 2);
        Map<String, String> params = new HashMap<>();
        for (String param : arguments.split(" -")) {
            int paramNameEnd = param.indexOf(" ");
            if (paramNameEnd == -1) {
                throw new IllegalArgumentException("Нет значения параметра " + param);
            }
            params.put(param.substring(0, paramNameEnd), param.substring(paramNameEnd).replace("\"", ""));
        }
        
        
        List<String> paramsNames = methodAndParams.paramsNames;
        
        if (paramsNames.size() != params.size()) {
            throw new IllegalArgumentException("Не правильное количество параметров. Должно быть " + paramsNames.size());
        }
        Object[] objects = new Object[paramsNames.size() + 1];
        
        for (int i = 0; i < paramsNames.size(); i++) {
            String paramValue = params.get(paramsNames.get(i));
            if (paramValue == null) {
                throw new IllegalArgumentException("Не передан параметр - " + paramsNames.get(i));
            }
            objects[i] = paramValue.trim();
        }
        objects[paramsNames.size()] = chatId;
        
        return (String) methodAndParams.method.invoke(handler, objects);
    }
    
    record MethodAndParams(Method method, List<String> paramsNames) {
    }
}
