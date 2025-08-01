const API_BASE_URL = 'http://localhost:8080/api/portfolio';
const CASH_API_BASE_URL = 'http://localhost:8080/api/cash';

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