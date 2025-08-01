const API_BASE_URL = 'http://localhost:8080/api/portfolio';
const CASH_API_BASE_URL = 'http://localhost:8080/api/cash';
const TRADE_HISTORY_API_BASE_URL = 'http://localhost:8080/api/trade-history';
const STOCK_DATA_API_BASE_URL = 'http://localhost:8080/api/stock-data';

export interface PortfolioStats {
  totalAssets: string;
  investments: string;
  daysGain: string;
  daysGainPercentage: string;
  cash: string;
}

export interface CashBalance {
  balance: number;
  formattedBalance: string;
}

export interface TradeHistory {
  id: number;
  tradeDate: string;
  ticker: string;
  quantity: number;
  price: number;
  tradeType: string;
  totalValue: number;
}

export interface StockData {
  symbol: string;
  name?: string;
  price?: number;
  currency?: string;
  marketCap?: number;
  previousClose?: number;
  dayGain?: number;
  dayGainPercent?: number;
  volume?: number;
  dayLow?: number;
  dayHigh?: number;
  yearLow?: number;
  yearHigh?: number;
  marketStatus?: string;
  error?: string;
}

export interface PortfolioItem {
  id: number;
  ticker: string;
  quantity: number;
  buyPrice: number;
  buyDate: string;
  totalValue: number;
}

class ApiService {
  private async request<T>(url: string, options?: RequestInit): Promise<T> {
    const response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
      },
      ...options,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
  }

  async getPortfolioStats(): Promise<PortfolioStats> {
    return this.request<PortfolioStats>(`${API_BASE_URL}/stats`);
  }

  async getCashBalance(): Promise<CashBalance> {
    return this.request<CashBalance>(`${CASH_API_BASE_URL}`);
  }

  async getAllTradeHistory(): Promise<TradeHistory[]> {
    return this.request<TradeHistory[]>(`${TRADE_HISTORY_API_BASE_URL}`);
  }

  async getTradesByType(tradeType: string): Promise<TradeHistory[]> {
    return this.request<TradeHistory[]>(`${TRADE_HISTORY_API_BASE_URL}/type/${tradeType}`);
  }

  async getTradesByTicker(ticker: string): Promise<TradeHistory[]> {
    return this.request<TradeHistory[]>(`${TRADE_HISTORY_API_BASE_URL}/ticker/${ticker}`);
  }

  async getStockData(symbols: string[]): Promise<StockData[]> {
    return this.request<StockData[]>(`${STOCK_DATA_API_BASE_URL}`, {
      method: 'POST',
      body: JSON.stringify({ symbols }),
    });
  }

  async addTrade(trade: Omit<TradeHistory, 'id' | 'totalValue'>): Promise<TradeHistory> {
    return this.request<TradeHistory>(`${TRADE_HISTORY_API_BASE_URL}`, {
      method: 'POST',
      body: JSON.stringify(trade),
    });
  }

  async getAllPortfolioItems(): Promise<PortfolioItem[]> {
    return this.request<PortfolioItem[]>(`${API_BASE_URL}`);
  }

  async addPortfolioItem(item: Omit<PortfolioItem, 'id' | 'totalValue'>): Promise<PortfolioItem> {
    return this.request<PortfolioItem>(`${API_BASE_URL}`, {
      method: 'POST',
      body: JSON.stringify(item),
    });
  }

  async updatePortfolioItem(id: number, item: Partial<PortfolioItem>): Promise<PortfolioItem> {
    return this.request<PortfolioItem>(`${API_BASE_URL}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(item),
    });
  }

  async deletePortfolioItem(id: number): Promise<void> {
    return this.request<void>(`${API_BASE_URL}/${id}`, {
      method: 'DELETE',
    });
  }
}

export const apiService = new ApiService(); 