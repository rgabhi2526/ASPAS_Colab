package com.aspas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Maps a calendar date to MongoDB {@link Date} bounds using a configurable zone
 * (default: JVM system default). Aligns day queries with how users expect "today"
 * in their region vs raw {@link LocalDateTime} conversion quirks.
 */
@Component
public class BusinessDateBounds {

    @Value("${aspas.business-timezone:}")
    private String timezone;

    public ZoneId zoneId() {
        if (timezone != null && !timezone.isBlank()) {
            return ZoneId.of(timezone.trim());
        }
        return ZoneId.systemDefault();
    }

    public Date startOfCalendarDay(LocalDate date) {
        return Date.from(date.atStartOfDay(zoneId()).toInstant());
    }

    public Date endOfCalendarDay(LocalDate date) {
        LocalDateTime end = date.atTime(23, 59, 59, 999_000_000);
        return Date.from(end.atZone(zoneId()).toInstant());
    }

    /**
     * Converts an instant in time to the same clock fields in the business zone (for bucketing).
     */
    public ZonedDateTime toZoned(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(zoneId());
    }

    public Date toMongoDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(zoneId()).toInstant());
    }
}
