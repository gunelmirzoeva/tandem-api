package com.example.tandem_api.util;

import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class TimeConverter {
    public LocalTime localToUTC(LocalTime localTime, ZoneId zoneId) {
        LocalDate arbitraryDate = LocalDate.of(2000, 1, 1);
        ZonedDateTime zonedLocal = ZonedDateTime.of(arbitraryDate, localTime, zoneId);

        return zonedLocal.withZoneSameInstant(ZoneOffset.UTC).toLocalTime();
    }

    public LocalTime utcToLocal(LocalTime utcTime, ZoneId zoneId) {
        LocalDate arbitraryDate = LocalDate.of(2000, 1, 1);
        ZonedDateTime zonedUtc = ZonedDateTime.of(arbitraryDate, utcTime, ZoneOffset.UTC);
        return zonedUtc.withZoneSameInstant(zoneId).toLocalTime();
    }
}
