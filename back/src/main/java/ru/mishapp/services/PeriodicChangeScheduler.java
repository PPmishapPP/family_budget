package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.mishapp.repository.PeriodicChangeRuleRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PeriodicChangeScheduler {

    private final PeriodicChangeRuleRepository periodicChangeRuleRepository;


    @Scheduled(cron = "${schedule.periodic-rule}")
    public void execute() {


        periodicChangeRuleRepository.findAll().forEach(rule -> {
                    while (!rule.getNextDay().isAfter(LocalDate.now())) {
                        
                    }
                }
        );
    }
}
