package ru.mishapp;

import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class Config {

    @Bean
    @ConfigurationProperties(prefix = "telegram.bot")
    public TelegramBotConfigurationProperty property() {
        return new TelegramBotConfigurationProperty();
    }

    @Bean
    @SneakyThrows
    public Bot bot(TelegramBotConfigurationProperty property, HandlerMapping handlerMapping) {
        Bot bot = new Bot(property.getToken(), property.getName(), handlerMapping);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
        return bot;
    }

    @Bean
    public HandlerMapping handlerMapping(ApplicationContext context) {
        Map<String, Object> beansWithAnnotation = context.getBeansWithAnnotation(TelegramHandler.class);
        Map<String, MethodMapping> methodMappingMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Class<?> handlerClass = entry.getValue().getClass();
            Map<String, Method> map = new HashMap<>();
            for (Method method : handlerClass.getMethods()) {
                TelegramCommand commandAnnotation = method.getAnnotation(TelegramCommand.class);
                if (commandAnnotation != null) {
                    String command = commandAnnotation.value();
                    map.put(command, method);
                }
            }
            if (!map.isEmpty()) {
                MethodMapping methodMapping = new MethodMapping(map, entry.getValue());
                methodMappingMap.put(entry.getKey(), methodMapping);
            }
        }
        return new HandlerMapping(methodMappingMap);
    }

}
