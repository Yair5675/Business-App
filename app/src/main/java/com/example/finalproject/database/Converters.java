package com.example.finalproject.database;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Converters {
    @TypeConverter
    public static LocalDate fromLongEpoch(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @TypeConverter
    public static Long fromDate(LocalDate date) {
        if (date == null) return null;

        final ZonedDateTime zdt = ZonedDateTime.of(date.atStartOfDay(), ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }
}
