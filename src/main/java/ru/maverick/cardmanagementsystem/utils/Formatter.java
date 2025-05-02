package ru.maverick.cardmanagementsystem.utils;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class Formatter {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    }
}