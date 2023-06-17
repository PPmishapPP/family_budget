package ru.mishapp.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.IBot;
import ru.mishapp.services.RuleExecuteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PeriodicChangeScheduler {
    
    private final RuleExecuteService ruleExecuteService;
    private final IBot bot;
    
    @Scheduled(cron = "${schedule.periodic-rule}")
    @Transactional
    public void execute() {
        Map<Long, List<String>> messages = ruleExecuteService.ruleExecute(LocalDate.now());
        for (Map.Entry<Long, List<String>> entry : messages.entrySet()) {
            Long chatId = entry.getKey();
            List<String> messageList = entry.getValue();
            for (String message : messageList) {
                bot.sendMessage(message, chatId);
            }
        }
    }
}
