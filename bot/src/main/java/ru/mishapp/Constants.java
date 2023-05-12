package ru.mishapp;

import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    
    public static final DateTimeFormatter DAY_AND_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
}