package ru.mishapp.handlers;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.services.RuleExecuteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@TelegramHandler(value = "будущее", description = "Провести будущие платежи заранее")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class FutureHandler {
    
    private final RuleExecuteService ruleExecuteService;
    
    @TelegramCommand("завтра")
    public String tomorrow(Long chatId) {
        Map<Long, List<String>> messages = ruleExecuteService.ruleExecute(LocalDate.now().plusDays(1));
        return String.join("\n", messages.getOrDefault(chatId, List.of()));
    }
}
