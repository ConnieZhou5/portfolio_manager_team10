// API Configuration
export const API_BASE = process.env.REACT_APP_API_BASE || "portfoliomanagerteam10-production.up.railway.app";
// API Endpoints
export const API_ENDPOINTS = {
  PORTFOLIO: `${API_BASE}/api/portfolio`,
  CASH: `${API_BASE}/api/cash`,
  TRADE_HISTORY: `${API_BASE}/api/trade-history`,
  STOCK_DATA: `${API_BASE}/api/stock-data`,
  BUY: `${API_BASE}/api/buy`,
  SELL: `${API_BASE}/api/sell`,
  PNL: `${API_BASE}/api/pnl`,
  ANALYSIS: `${API_BASE}/api/analysis`,
} as const; 