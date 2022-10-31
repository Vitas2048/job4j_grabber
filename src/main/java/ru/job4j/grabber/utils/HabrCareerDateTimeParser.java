package ru.job4j.grabber.utils;

import ru.job4j.grabber.HabrCareerParse;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        ZonedDateTime p = ZonedDateTime.parse(parse);
        return p.toLocalDateTime();
    }
}
