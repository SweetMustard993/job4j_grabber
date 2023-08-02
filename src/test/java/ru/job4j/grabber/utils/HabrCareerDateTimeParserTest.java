package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    @Test
    void parseWhenDateContainsTimeZone() {
        String datetime = "2023-07-31T15:57:41+03:00";
        DateTimeParser dtp = new HabrCareerDateTimeParser();
        LocalDateTime res = dtp.parse(datetime);
        assertEquals("2023-07-31T15:57:41", res.toString());
    }
}
