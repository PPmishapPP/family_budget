package ru.mishapp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    
    public static final DateTimeFormatter DAY_AND_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    public static final NumberFormat RUB = NumberFormat.getInstance(new Locale("ru", "RU"));
}