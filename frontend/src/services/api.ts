const API_BASE_URL = 'http://localhost:8080/api/portfolio';

export interface PortfolioStats {
  totalAssets: string;
  investments: string;
  daysGain: string;
  daysGainPercentage: string;
  cash: string;
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
  private async request<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
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
    return this.request<PortfolioStats>('/stats');
  }

  async getAllPortfolioItems(): Promise<PortfolioItem[]> {
    return this.request<PortfolioItem[]>('');
  }

  async addPortfolioItem(item: Omit<PortfolioItem, 'id' | 'totalValue'>): Promise<PortfolioItem> {
    return this.request<PortfolioItem>('', {
      method: 'POST',
      body: JSON.stringify(item),
    });
  }

  async updatePortfolioItem(id: number, item: Partial<PortfolioItem>): Promise<PortfolioItem> {
    return this.request<PortfolioItem>(`/${id}`, {
      method: 'PUT',
      body: JSON.stringify(item),
    });
  }

  async deletePortfolioItem(id: number): Promise<void> {
    return this.request<void>(`/${id}`, {
      method: 'DELETE',
    });
  }
}

export const apiService = new ApiService(); 