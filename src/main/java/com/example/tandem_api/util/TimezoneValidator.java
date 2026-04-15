package com.example.tandem_api.util;

import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class TimezoneValidator {

    public boolean isValidTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return false;
        }
        try {
            ZoneId.of(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
