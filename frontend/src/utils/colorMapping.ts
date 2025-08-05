// Color mapping utility for stock tickers
const colors = [
    '#a855f7', // Purple
    '#3b82f6', // Blue
    '#f97316', // Orange
    '#ec4899', // Pink
    '#10b981', // Green
    '#f59e0b', // Yellow
    '#ef4444', // Red
    '#8b5cf6', // Violet
    '#06b6d4', // Cyan
    '#84cc16', // Lime
    '#f43f5e', // Rose
    '#34d399', // Emerald
    '#f472b6', // Pink
    '#60a5fa', // Blue
    '#a78bfa', // Violet
    '#fb7185', // Rose
    '#a3e635', // Lime
    '#fbbf24', // Amber
    '#06b6d4', // Sky
    '#f97316', // Orange
];

// Map to store ticker to color assignments
const tickerColorMap = new Map<string, string>();
let nextColorIndex = 0;

/**
 * Get a consistent color for a stock ticker
 * @param ticker Stock ticker symbol
 * @returns Hex color string
 */
export const getTickerColor = (ticker: string): string => {
    // If we already have a color for this ticker, return it
    if (tickerColorMap.has(ticker)) {
        return tickerColorMap.get(ticker)!;
    }
    
    // Assign the next available color
    const color = colors[nextColorIndex % colors.length];
    tickerColorMap.set(ticker, color);
    nextColorIndex++;
    
    return color;
};

/**
 * Clear the color mapping cache (useful for testing)
 */
export const clearColorMapping = (): void => {
    tickerColorMap.clear();
    nextColorIndex = 0;
}; 