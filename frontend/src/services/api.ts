import { API_ENDPOINTS } from '../config/api';

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

export interface BuyRequest {
  ticker: string;
  quantity: number;
  price: number;
  tradeDate?: string;
}

export interface SellRequest {
  ticker: string;
  quantity: number;
  price: number;
  tradeDate?: string;
}

export interface BuyResponse {
  success: boolean;
  message?: string;
  error?: string;
  totalCost?: number;
  remainingCash?: number;
  portfolioItem?: any;
  tradeRecord?: any;
}

export interface SellResponse {
  success: boolean;
  message?: string;
  error?: string;
  totalProceeds?: number;
  remainingCash?: number;
  portfolioItem?: any;
  tradeRecord?: any;
}

export interface PortfolioItem {
  id: number;
  ticker: string;
  quantity: number;
  buyPrice: number;
  buyDate: string;
  totalValue: number;
}

export interface MonthlyPnLData {
  month: string;
  realized: number;
  unrealized: number;
}

export interface PnLResponse {
  monthlyData: MonthlyPnLData[];
  totalRealized: number;
  totalUnrealized: number;
  totalPnL: number;
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
    return this.request<PortfolioStats>(`${API_ENDPOINTS.PORTFOLIO}/stats`);
  }

  async getCashBalance(): Promise<CashBalance> {
    return this.request<CashBalance>(`${API_ENDPOINTS.CASH}`);
  }

  async addCash(amount: number): Promise<CashBalance> {
    return this.request<CashBalance>(`${API_ENDPOINTS.CASH}/add`, {
      method: 'POST',
      body: JSON.stringify({ amount }),
    });
  }

  async getAllTradeHistory(): Promise<TradeHistory[]> {
    return this.request<TradeHistory[]>(`${API_ENDPOINTS.TRADE_HISTORY}`);
  }

  async getTradesByType(tradeType: string): Promise<TradeHistory[]> {
    return this.request<TradeHistory[]>(`${API_ENDPOINTS.TRADE_HISTORY}/type/${tradeType}`);
  }

  async getTradesByTicker(ticker: string): Promise<TradeHistory[]> {
    return this.request<TradeHistory[]>(`${API_ENDPOINTS.TRADE_HISTORY}/ticker/${ticker}`);
  }

  async getStockData(symbols: string[]): Promise<StockData[]> {
    return this.request<StockData[]>(`${API_ENDPOINTS.STOCK_DATA}`, {
      method: 'POST',
      body: JSON.stringify({ symbols }),
    });
  }

  async executeBuy(buyRequest: BuyRequest): Promise<BuyResponse> {
    return this.request<BuyResponse>(`${API_ENDPOINTS.BUY}`, {
      method: 'POST',
      body: JSON.stringify(buyRequest),
    });
  }

  async executeSell(sellRequest: SellRequest): Promise<SellResponse> {
    return this.request<SellResponse>(`${API_ENDPOINTS.SELL}`, {
      method: 'POST',
      body: JSON.stringify(sellRequest),
    });
  }

  async addTrade(trade: Omit<TradeHistory, 'id' | 'totalValue'>): Promise<TradeHistory> {
    return this.request<TradeHistory>(`${API_ENDPOINTS.TRADE_HISTORY}`, {
      method: 'POST',
      body: JSON.stringify(trade),
    });
  }

  async getAllPortfolioItems(): Promise<PortfolioItem[]> {
    return this.request<PortfolioItem[]>(`${API_ENDPOINTS.PORTFOLIO}`);
  }

  async addPortfolioItem(item: Omit<PortfolioItem, 'id' | 'totalValue'>): Promise<PortfolioItem> {
    return this.request<PortfolioItem>(`${API_ENDPOINTS.PORTFOLIO}`, {
      method: 'POST',
      body: JSON.stringify(item),
    });
  }

  async updatePortfolioItem(id: number, item: Partial<PortfolioItem>): Promise<PortfolioItem> {
    return this.request<PortfolioItem>(`${API_ENDPOINTS.PORTFOLIO}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(item),
    });
  }

  async deletePortfolioItem(id: number): Promise<void> {
    return this.request<void>(`${API_ENDPOINTS.PORTFOLIO}/${id}`, {
      method: 'DELETE',
    });
  }

  async getMonthlyPnL(): Promise<PnLResponse> {
    return this.request<PnLResponse>(`${API_ENDPOINTS.PNL}/monthly`);
  }

  async getAnalysis(symbol: string): Promise<any> {
    return this.request<any>(`${API_ENDPOINTS.ANALYSIS}/${symbol}`);
  }
}

export const apiService = new ApiService(); 