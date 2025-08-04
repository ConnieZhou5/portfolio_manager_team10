import React, { useState } from 'react';
import axios from 'axios';
import { API_ENDPOINTS } from './config/api';

const PortfolioTable = () => {
  const [symbolsInput, setSymbolsInput] = useState('');
  const [portfolio, setPortfolio] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchPortfolio = async (symbols) => {
    try {
      setLoading(true);
      setError(null);
      const response = await axios.post(`${API_ENDPOINTS.STOCK_DATA}`, {
        symbols,
      });
      setPortfolio(response.data);
    } catch (err) {
      setError('Failed to fetch portfolio data.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const symbols = symbolsInput
      .split(',')
      .map((sym) => sym.trim().toUpperCase())
      .filter((sym) => sym !== '');
    fetchPortfolio(symbols);
  };

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Portfolio</h2>

      <form onSubmit={handleSubmit} className="mb-4">
        <input
          type="text"
          placeholder="Enter comma-separated symbols (e.g. AAPL,GOOG)"
          value={symbolsInput}
          onChange={(e) => setSymbolsInput(e.target.value)}
          className="border px-2 py-1 mr-2"
        />
        <button
          type="submit"
          className="bg-blue-500 text-white px-4 py-1 rounded hover:bg-blue-600"
        >
          Fetch
        </button>
      </form>

      {loading && <div>Loading portfolio...</div>}
      {error && <div className="text-red-500">{error}</div>}

      {portfolio.length > 0 && (
        <table className="table-auto w-full border-collapse border border-gray-300">
          <thead>
            <tr className="bg-gray-100">
              <th className="border px-4 py-2">Symbol</th>
              <th className="border px-4 py-2">Name</th>
              <th className="border px-4 py-2">Price</th>
              <th className="border px-4 py-2">Currency</th>
              <th className="border px-4 py-2">Market Cap</th>
            </tr>
          </thead>
          <tbody>
          {portfolio.map((stock) => (
            <tr key={stock.symbol} className={stock.error ? "bg-red-100" : ""}>
                <td className="border px-4 py-2">{stock.symbol}</td>
                <td className="border px-4 py-2">
                {stock.error ? `Error: ${stock.error}` : stock.name || 'N/A'}
                </td>
                <td className="border px-4 py-2">{stock.price ?? 'N/A'}</td>
                <td className="border px-4 py-2">{stock.currency ?? 'N/A'}</td>
                <td className="border px-4 py-2">{stock.marketCap?.toLocaleString() ?? 'N/A'}</td>
            </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default PortfolioTable;
