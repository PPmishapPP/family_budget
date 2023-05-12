package ru.mishapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BotApp {
    
    public static void main(String[] args) {
        SpringApplication.run(BotApp.class);
    }
}
