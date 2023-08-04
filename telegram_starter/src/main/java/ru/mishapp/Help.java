package ru.mishapp;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class Help implements IMethodMapping {
    
    private final Map<String, IMethodMapping> methodMappingMap;
    private final Map<String, String> descriptions;
    
    @Override
    public String execute(String text, Long chatId) {
        if (!StringUtils.hasText(text)) {
            StringBuilder builder = new StringBuilder("Доступные команды:\n");
            for (String command : methodMappingMap.keySet()) {
                builder.append(" * ");
                builder.append(command);
                String description = descriptions.get(command);
                if (StringUtils.hasText(description)) {
                    builder.append(" (");
                    builder.append(description);
                    builder.append(")");
                }
                builder.append("\n");
            }
            return builder.toString();
        }
        IMethodMapping iMethodMapping = methodMappingMap.get(text);
        return iMethodMapping.toTelegram();
    }
    
    @Override
    public String toTelegram() {
        return "Помощник";
    }
}
