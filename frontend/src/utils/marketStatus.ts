/**
 * Check if the market is currently open
 * Market hours: Monday-Friday, 9:30 AM - 4:00 PM ET
 * @returns boolean - true if market is open, false if closed
 */
export const isMarketOpen = (): boolean => {
  const now = new Date();
  const etTime = new Date(now.toLocaleString("en-US", {timeZone: "America/New_York"}));
  
  // Get day of week (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
  const dayOfWeek = etTime.getDay();
  
  // Market is closed on weekends
  if (dayOfWeek === 0 || dayOfWeek === 6) {
    return false;
  }
  
  // Get current time in ET
  const currentHour = etTime.getHours();
  const currentMinute = etTime.getMinutes();
  const currentTimeInMinutes = currentHour * 60 + currentMinute;
  
  // Market hours: 9:30 AM - 4:00 PM ET
  const marketOpenTime = 9 * 60 + 30; // 9:30 AM
  const marketCloseTime = 16 * 60; // 4:00 PM
  
  return currentTimeInMinutes >= marketOpenTime && currentTimeInMinutes < marketCloseTime;
}; 