package ru.mishapp.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PeriodicChangeScheduler {


    @Scheduled(cron = "${schedule.periodic-rule}")
    public void execute() {
        System.out.println("Работает!");
    }
}
