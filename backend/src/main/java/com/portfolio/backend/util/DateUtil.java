package com.portfolio.backend.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for handling timezone-aware date operations
 */
public class DateUtil {
    
    private static final ZoneId NYC = ZoneId.of("America/New_York");
    
    /**
     * Get current date in New York timezone
     * 
     * @return LocalDate representing today's date in New York timezone
     */
    public static LocalDate getCurrentDateInNYC() {
        ZonedDateTime nowInNYC = ZonedDateTime.now(NYC);
        return nowInNYC.toLocalDate();
    }
    
    /**
     * Get current datetime in New York timezone
     * 
     * @return LocalDateTime representing current time in New York timezone
     */
    public static LocalDateTime getCurrentDateTimeInNYC() {
        ZonedDateTime nowInNYC = ZonedDateTime.now(NYC);
        return nowInNYC.toLocalDateTime();
    }
    
    /**
     * Convert a date string to LocalDate in New York timezone
     * 
     * @param datNYCring Date string in YYYY-MM-DD format
     * @return LocalDate in New York timezone
     */
    public static LocalDate parseDateInNYC(String datNYCring) {
        if (datNYCring == null || datNYCring.trim().isEmpty()) {
            return getCurrentDateInNYC();
        }
        
        try {
            LocalDate parsedDate = LocalDate.parse(datNYCring);
            // Convert to New York timezone
            ZonedDateTime zonedDateTime = parsedDate.atStartOfDay(NYC);
            return zonedDateTime.toLocalDate();
        } catch (Exception e) {
            // If parsing fails, return current date in New York timezone
            return getCurrentDateInNYC();
        }
    }
} 