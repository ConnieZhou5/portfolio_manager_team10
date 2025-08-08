/**
 * Utility functions for handling dates in EST timezone
 */

/**
 * Get current date in EST timezone as YYYY-MM-DD string
 * 
 * @returns Date string in YYYY-MM-DD format in EST timezone
 */
export function getCurrentDateInNYC(): string {
    const now = new Date();
    
    // Convert to EST timezone
    const estDate = new Date(now.toLocaleString("en-US", {timeZone: "America/New_York"}));
    
    // Format as YYYY-MM-DD
    const year = estDate.getFullYear();
    const month = String(estDate.getMonth() + 1).padStart(2, '0');
    const day = String(estDate.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
}

/**
 * Format a date string to display format in EST/EDT timezone
 * 
 * @param dateString Date string in YYYY-MM-DD format
 * @returns Formatted date string for display
 */
export function formatDateInNYC(dateString: string): string {
    if (!dateString) return '';
    
    try {
        // Parse the date string and convert to EST
        const date = new Date(dateString + 'T00:00:00');
        const estDate = new Date(date.toLocaleString("en-US", {timeZone: "America/New_York"}));
        
        return estDate.toLocaleDateString('en-US', {
            month: 'numeric',
            day: 'numeric',
            year: 'numeric'
        });
    } catch (error) {
        console.error('Error formatting date:', error);
        return dateString;
    }
} 