package ru.mishapp.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constans {
    
    public static final DateTimeFormatter DAY_AND_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
}
