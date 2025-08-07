package com.portfolio.backend.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for handling timezone-aware date operations
 */
public class DateUtil {
    
    private static final ZoneId EST_ZONE = ZoneId.of("America/New_York");
    
    /**
     * Get current date in EST timezone
     * 
     * @return LocalDate representing today's date in EST
     */
    public static LocalDate getCurrentDateInEST() {
        ZonedDateTime nowInEST = ZonedDateTime.now(EST_ZONE);
        return nowInEST.toLocalDate();
    }
    
    /**
     * Convert a date string to LocalDate in EST timezone
     * 
     * @param dateString Date string in YYYY-MM-DD format
     * @return LocalDate in EST timezone
     */
    public static LocalDate parseDateInEST(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return getCurrentDateInEST();
        }
        
        try {
            LocalDate parsedDate = LocalDate.parse(dateString);
            // Convert to EST timezone
            ZonedDateTime zonedDateTime = parsedDate.atStartOfDay(EST_ZONE);
            return zonedDateTime.toLocalDate();
        } catch (Exception e) {
            // If parsing fails, return current date in EST
            return getCurrentDateInEST();
        }
    }
} 