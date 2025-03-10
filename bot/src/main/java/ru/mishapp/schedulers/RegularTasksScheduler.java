package ru.mishapp.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.IBot;
import ru.mishapp.services.RegularTasksNotificationService;

@Service
@RequiredArgsConstructor
public class RegularTasksScheduler {

    private final IBot iBot;

    private final RegularTasksNotificationService regularTasksNotificationService;

    @Scheduled(cron = "${schedule.notifications}")
    @Transactional
    public void execute() {
        regularTasksNotificationService.getActualNotifications()
                .forEach(n -> iBot.sendMessage(n.description(), n.chatId()));
    }
}
