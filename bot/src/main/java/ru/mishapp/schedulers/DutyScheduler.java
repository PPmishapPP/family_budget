package ru.mishapp.schedulers;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.Constants;
import ru.mishapp.IBot;
import ru.mishapp.dto.Change;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.DutyUser;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.DutyRepository;
import ru.mishapp.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DutyScheduler {
    
    private final DutyRepository dutyRepository;
    private final UserRepository userRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    private final Random random = new Random();
    private final IBot bot;
    
    @Scheduled(cron = "${schedule.duty}")
    @Transactional
    public void execute() {
        dutyRepository.findAll().forEach(duty -> {
            
            LocalDate lastMessages = duty.getLastMessages();
            int days = (int) Duration.between(lastMessages.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays();
            
            if (days > 0) {
                List<DutyUser> users = duty.getUsers();
                Long nextUserId = users.get(random.nextInt(users.size())).getId().getId();
                if (nextUserId != null) {
                    userRepository.findById(nextUserId).ifPresent(user -> {
                        AccountHistory last = accountHistoryRepository.findLast(duty.getDutyAccountId());
                        AccountHistory save = accountHistoryRepository.save(
                            last.applyChange(new Change("", duty.getAward() * days, "на зарплату"))
                        );
                        
                        dutyRepository.updateNext(duty.getId(), nextUserId);
                        
                        String message = String.format(
                            "%s, сегодня у тебя есть возможность помыть посуду и заработать %s₽",
                            user.getName(),
                            Constants.RUB.format(save.getBalance())
                        );
                        bot.sendMessage(message, duty.getChatId());
                    });
                }
            }
            
        });
        
        
    }
}
