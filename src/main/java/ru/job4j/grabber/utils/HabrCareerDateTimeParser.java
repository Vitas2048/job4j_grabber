package ru.job4j.grabber.utils;

import ru.job4j.grabber.HabrCareerParse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        parse = parse.substring(0, 19);
        String[] ss = parse.split("-");
        int year = Integer.parseInt(ss[0]);
        int month = Integer.parseInt(ss[1]);
        ss = ss[2].split(":");
        int day = Integer.parseInt(ss[0].substring(0, 1));
        int hour = Integer.parseInt(ss[0].substring(3, 4));
        int minute = Integer.parseInt(ss[1]);
        int seconds = Integer.parseInt(ss[2]);
        return LocalDateTime.of(year, month, day, hour, minute, seconds);
    }

}
