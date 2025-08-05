import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import PivotTable from './PivotTable';
import { apiService, TradeHistory, PortfolioItem, SellRequest, SellResponse } from '../services/api';
import { usePortfolio } from '../context/PortfolioContext';
import { isMarketOpen } from '../utils/marketStatus';

type Market = 'Market Open' | 'Market Closed'

interface PortfolioDataItem {
    symbol: string;
    lastPrice: number;
    change: number;
    changePercent: number;
    quantity: number;
    pricePaid: number;
    daysGain: number;
    totalGain: number;
    totalGainPercent: number;
    value: number;
    date: string;
}

const Sells = () => {
    const [symbol, setSymbol] = useState('');
    const [quantity, setQuantity] = useState('');
    const [orderType, setOrderType] = useState('Market');
    const isQuantityInvalid = parseFloat(quantity) <= 0 || isNaN(parseFloat(quantity));
    const [invalidqty, setQtyError] = useState(false);
    const [invalidstock, setStockError] = useState(false);
    const [sellTrades, setSellTrades] = useState<TradeHistory[]>([]);
    const [portfolioItems, setPortfolioItems] = useState<PortfolioItem[]>([]);
    const [portfolioData, setPortfolioData] = useState<PortfolioDataItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [marketOpen, setMarketOpen] = useState(true);
    const [sellLoading, setSellLoading] = useState(false);
    const [sellError, setSellError] = useState<string | null>(null);
    const [sellSuccess, setSellSuccess] = useState<string | null>(null);
    const [isExpanded, setIsExpanded] = useState(true);
    const { refreshTrigger, triggerRefresh } = usePortfolio();

    // Auto-dismiss success and error messages after 10 seconds
    useEffect(() => {
        if (sellSuccess) {
            const timer = setTimeout(() => {
                setSellSuccess(null);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [sellSuccess]);

    useEffect(() => {
        if (sellError) {
            const timer = setTimeout(() => {
                setSellError(null);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [sellError]);

    // Auto-dismiss validation error messages after 10 seconds
    useEffect(() => {
        if (invalidqty) {
            const timer = setTimeout(() => {
                setQtyError(false);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [invalidqty]);

    useEffect(() => {
        if (invalidstock) {
            const timer = setTimeout(() => {
                setStockError(false);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [invalidstock]);

    // Check market status every minute
    useEffect(() => {
        const checkMarketStatus = () => {
            setMarketOpen(isMarketOpen());
        };

        // Check immediately
        checkMarketStatus();

        // Check every minute
        const interval = setInterval(checkMarketStatus, 60000);

        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                // Fetch both sell trades and portfolio items
                const [trades, portfolio] = await Promise.all([
                    apiService.getTradesByType('SELL'),
                    apiService.getAllPortfolioItems()
                ]);

                setSellTrades(trades);
                setPortfolioItems(portfolio);

                // Convert portfolio items to display format
                const convertedData = await convertPortfolioToDisplayData(portfolio);
                setPortfolioData(convertedData);

            } catch (err) {
                setError('Failed to load data');
                console.error('Error fetching data:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [refreshTrigger]); // Add refreshTrigger as dependency

    // Convert portfolio items to display format with current stock prices
    const convertPortfolioToDisplayData = async (portfolio: PortfolioItem[]): Promise<PortfolioDataItem[]> => {
        if (portfolio.length === 0) return [];

        try {
            // Get current stock prices for all portfolio items
            const symbols = portfolio.map(item => item.ticker);
            const stockData = await apiService.getStockData(symbols);

            return portfolio.map(item => {
                const currentStock = stockData.find(stock => stock.symbol === item.ticker);
                const currentPrice = currentStock?.price || item.buyPrice;
                const previousClose = currentStock?.previousClose || item.buyPrice;

                const priceChange = currentPrice - previousClose;
                const priceChangePercent = previousClose > 0 ? (priceChange / previousClose) * 100 : 0;
                const totalGain = (currentPrice - item.buyPrice) * item.quantity;
                const totalGainPercent = item.buyPrice > 0 ? ((currentPrice - item.buyPrice) / item.buyPrice) * 100 : 0;
                const currentValue = currentPrice * item.quantity;

                return {
                    symbol: item.ticker,
                    lastPrice: currentPrice,
                    change: priceChange,
                    changePercent: priceChangePercent,
                    quantity: item.quantity,
                    pricePaid: item.buyPrice,
                    daysGain: priceChange * item.quantity,
                    totalGain: totalGain,
                    totalGainPercent: totalGainPercent,
                    value: currentValue,
                    date: item.buyDate
                };
            });
        } catch (err) {
            console.error('Error fetching stock data:', err);
            // Return portfolio data without current prices if stock data fetch fails
            return portfolio.map(item => ({
                symbol: item.ticker,
                lastPrice: item.buyPrice,
                change: 0,
                changePercent: 0,
                quantity: item.quantity,
                pricePaid: item.buyPrice,
                daysGain: 0,
                totalGain: 0,
                totalGainPercent: 0,
                value: item.totalValue,
                date: item.buyDate
            }));
        }
    };

    const getMarketStatusStyles = () => {
        if (marketOpen) {
            return {
                containerClass: 'bg-green-50 border-green-200',
                dotClass: 'bg-green-500 animate-pulse',
                textClass: 'text-green-700'
            };
        } else {
            return {
                containerClass: 'bg-red-50 border-red-200',
                dotClass: 'bg-red-500',
                textClass: 'text-red-700'
            };
        }
    };

    const calculateTotal = () => {
        const stock = portfolioData.find(item => item.symbol === symbol);
        const price = stock ? stock.lastPrice : 0;
        const qty = parseFloat(quantity) || 0;
        return (price * qty).toFixed(2);
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            month: 'numeric',
            day: 'numeric',
            year: 'numeric'
        });
    };

    const [expandedRows, setExpandedRows] = useState<{ [key: string]: boolean }>({});
    const [searchQuery, setSearchQuery] = useState<string>(''); // State for search query

    const toggleRow = (symbol: string) => {
        setExpandedRows(prev => ({ ...prev, [symbol]: !prev[symbol] }));
    };

    // Filter data based on the search query
    const filteredData = portfolioData.filter(row => row.symbol.toLowerCase().includes(searchQuery.toLowerCase()));

    const handleSellTransaction = async () => {
        if (!symbol || !quantity || parseFloat(quantity) <= 0) {
            setSellError('Please select a stock and enter a valid quantity');
            return;
        }

        if (!marketOpen) {
            setSellError('Market is closed. Cannot sell at this time.');
            return;
        }

        // Find the stock in portfolio data
        const stock = portfolioData.find(item => item.symbol === symbol);
        if (!stock) {
            setSellError('Stock not found in your portfolio');
            return;
        }

        const qty = parseInt(quantity);
        if (qty > stock.quantity) {
            setSellError(`Insufficient shares. Available: ${stock.quantity}, Requested: ${qty}`);
            return;
        }

        try {
            setSellLoading(true);
            setSellError(null);
            setSellSuccess(null);

            const sellRequest: SellRequest = {
                ticker: symbol,
                quantity: qty,
                price: stock.lastPrice,
                tradeDate: new Date().toISOString().split('T')[0] // Today's date in YYYY-MM-DD format
            };

            const response = await apiService.executeSell(sellRequest);

            if (response.success) {
                setSellSuccess(`Successfully sold ${qty} shares of ${symbol} for $${response.totalProceeds?.toFixed(2)}`);
                setQuantity('');
                setSymbol('');

                // Refresh portfolio data and trades
                const [updatedTrades, updatedPortfolio] = await Promise.all([
                    apiService.getTradesByType('SELL'),
                    apiService.getAllPortfolioItems()
                ]);

                setSellTrades(updatedTrades);
                setPortfolioItems(updatedPortfolio);

                // Convert updated portfolio to display format
                const convertedData = await convertPortfolioToDisplayData(updatedPortfolio);
                setPortfolioData(convertedData);

                // Trigger portfolio refresh to update Stats and Assets components
                triggerRefresh();
            } else {
                setSellError(response.error || 'Sell transaction failed');
            }
        } catch (err) {
            setSellError('Failed to execute sell transaction');
            console.error('Error executing sell transaction:', err);
        } finally {
            setSellLoading(false);
        }
    };

    return (
        <div className="bg-white rounded-3xl p-8 max-w-6xl mx-auto shadow-lg">
            {/* Header Tabs */}
            <div className="border-b border-gray-200 mb-8">
                <div className="flex space-x-8">
                    <Link to="/Positions">
                        <button className="text-gray-500 hover:text-gray-700 pb-2 px-1 font-medium">
                            Buy
                        </button>
                    </Link>
                    <Link to="/Positions/Sell">
                        <button className="text-orange-500 border-b-2 border-orange-500 pb-2 px-1 font-medium">
                            Sell
                        </button>
                    </Link>
                </div>
            </div>

            <div className={`grid gap-8 transition-all duration-300 ${isExpanded ? 'grid-cols-1 lg:grid-cols-3' : 'grid-cols-1'}`}>
                {/* Left Column - Stock Info */}
                <div className={`transition-all duration-300 ${isExpanded ? 'lg:col-span-2' : 'col-span-1'}`}>
                    {/* Search Bar */}
                    <div className="relative mb-6">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center">
                            <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                            </svg>
                        </div>
                        <input
                            type="text"
                            className="rounded-3xl block w-full pl-10 pr-3 py-3 rounded-lg bg-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                            placeholder="Search stocks..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                    </div>

                    <div className={`flex justify-end mb-4`}>
                        <div className={`flex items-center space-x-2 px-3 py-1.5 rounded-full border ${getMarketStatusStyles().containerClass}`}>
                            <div className={`w-2 h-2 rounded-full ${getMarketStatusStyles().dotClass}`}></div>
                            <span className={`text-sm font-medium ${getMarketStatusStyles().textClass}`}>
                                {marketOpen ? 'Market Open' : 'Market Closed'}
                            </span>
                        </div>
                    </div>

                    {/* Table Section */}
                    <div className="relative">
                        <div className="col-span-2 bg-white rounded-2xl p-6 shadow-md text-xs">
                            {loading ? (
                                <div className="flex items-center justify-center h-32">
                                    <div className="text-gray-500">Loading portfolio data...</div>
                                </div>
                            ) : error ? (
                                <div className="flex items-center justify-center h-32">
                                    <div className="text-red-500">{error}</div>
                                </div>
                            ) : portfolioData.length === 0 ? (
                                <div className="flex items-center justify-center h-32">
                                    <div className="text-gray-500">No portfolio holdings found</div>
                                </div>
                            ) : (
                                            <PivotTable data={portfolioData} searchText={searchQuery} />
                                        )}
                        </div>


                        <div className="absolute top-44 -right-7 z-10">
                            {isExpanded ? (
                                <button
                                    onClick={() => setIsExpanded(false)}
                                    className="bg-orange-500 hover:bg-orange-600 text-white p-2 rounded-full shadow-lg transition-colors"
                                    aria-label="Close trading panel"
                                >
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                    </svg>
                                </button>
                            ) : (
                                    <button
                                        onClick={() => setIsExpanded(true)}
                                        className="bg-orange-500 hover:bg-orange-600 text-white p-2 rounded-full shadow-lg transition-colors"
                                        aria-label="Open trading panel"
                                    >
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                                        </svg>
                                    </button>
                                )}
                        </div>
                    </div>
                </div>

                {/* Right Column - Sell Form and Transactions (Sticky) */}
                {isExpanded && (
                    <div className="space-y-6 animate-in slide-in-from-right duration-600 bg-white border-l border-gray-200 pl-6 -mr-8 pr-8 rounded-r-3xl">

                        {/* Sell Form */}

                        <div className="bg-white border border-gray-100 rounded-3xl p-8 space-y-6 shadow-sm">

                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-lg font-semibold text-gray-800">Sell Order</h3>
                            </div>

                            {/* Symbol Input */}
                            <div className="flex">
                                <label className="block text-md text-gray-700 mt-2">Symbol</label>
                                <input
                                    type="text"
                                    className="rounded-3xl text-center ml-[59px] w-full px-3 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                                    value={symbol}
                                    onChange={(e) => {
                                        const value = e.target.value;
                                        const stock = portfolioData.find(item => item.symbol === value);

                                        if (stock && stock.quantity > 0) {
                                            setSymbol(value);
                                            setStockError(false);
                                        } else {
                                            setSymbol(value);
                                            setStockError(true);
                                        }
                                    }}
                                />
                            </div>

                            {/* Quantity Input */}
                            <div className="flex">
                                <label className="block text-md text-gray-700 mt-2">Quantity</label>
                                <input
                                    type="number"
                                    className="rounded-3xl text-center ml-[51px] w-full px-3 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                                    value={quantity}
                                    onChange={(e) => {
                                        const value = e.target.value;
                                        if (value === '' || (Number(value) >= 0 && Number.isInteger(Number(value)))) {
                                            setQuantity(value);
                                            setQtyError(false);
                                        }
                                    }}
                                    placeholder="0"
                                />
                            </div>

                            {/* Order Type */}
                            <div className="flex">
                                <label className="block text-md text-gray-700 mt-2">Order</label>
                                <div className="rounded-3xl ml-[70px] w-full px-3 pr-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 font-medium">
                                    {orderType}
                                </div>
                            </div>

                            {/* Total */}
                            <div className="flex">
                                <label className="block text-md text-gray-700 mt-2">Total~</label>
                                <div className="rounded-3xl ml-[66px] w-full px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 font-medium">
                                    ${calculateTotal()}
                                </div>
                            </div>

                            {/* Error/Success Messages */}
                            {sellError && (
                                <div className="bg-red-100 text-red-700 text-sm p-2 rounded-lg">
                                    ❌ {sellError}
                                </div>
                            )}

                            {sellSuccess && (
                                <div className="bg-green-100 text-green-700 text-sm p-2 rounded-lg">
                                    ✅ {sellSuccess}
                                </div>
                            )}

                            {invalidqty && (
                                <div className="bg-red-100 text-red-700 text-sm p-2 rounded-lg">
                                    ❌ Not Valid Quantity
                                </div>
                            )}

                            {invalidstock && (
                                <div className="bg-red-100 text-red-700 text-sm p-2 rounded-lg">
                                    ❌ Not Valid Stock
                                </div>
                            )}

                            {/* Market Status Indicator */}
                            {!marketOpen && (
                                <div className="bg-red-50 border border-red-200 rounded-lg p-2 text-center">
                                    <span className="text-red-700 text-sm font-medium">Market Closed - Cannot Sell</span>
                                </div>
                            )}

                            {/* Action Buttons */}
                            <div className="flex space-x-5 pt-4">
                                <button
                                    className={`rounded-3xl flex-1 font-medium py-3 px-6 transition-colors ${!symbol || parseFloat(quantity) <= 0 || !marketOpen || sellLoading
                                        ? 'bg-gray-400 text-gray-600 cursor-not-allowed'
                                        : 'bg-orange-500 hover:bg-orange-600 text-white'
                                        } ${!marketOpen ? 'hover:cursor-not-allowed' : ''}`}
                                    disabled={!symbol || parseFloat(quantity) <= 0 || !marketOpen || sellLoading}
                                    onClick={handleSellTransaction}
                                >
                                    {sellLoading ? 'Processing...' : 'Sell'}
                                </button>
                                <button className="rounded-3xl flex-1 border border-gray-300 bg-white hover:bg-gray-200 text-gray-700 font-medium py-3 px-6 transition-colors"
                                    onClick={() => {
                                        setQuantity('');
                                        setSymbol('');
                                        setQtyError(false);
                                        setStockError(false);
                                        setSellError(null);
                                        setSellSuccess(null);
                                    }}>
                                    Cancel
                            </button>
                            </div>
                        </div>


                        {/* Sell Transactions Log */}
                        <div className="rounded-3xl bg-white border border-gray-100 overflow-hidden shadow-sm">
                            <div className="flex items-center justify-between px-6 pt-6 pb-2 mb-1 border-b border-gray-100 ">
                                <h3 className="text-gray-600 font-medium">Sell Transactions Log</h3>


                            </div>
                            <div className="max-h-96 overflow-y-auto">
                                {loading ? (
                                    <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left">
                                        Loading sell transactions...
                                    </div>
                                ) : error ? (
                                    <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-red-500 text-left">
                                        {error}
                                    </div>
                                ) : sellTrades.length === 0 ? (
                                    <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left">
                                        No sell transactions found
                                    </div>
                                ) : (
                                                sellTrades.map((trade, index) => (
                                                    <div key={trade.id} className={`pl-6 pr-6 pt-2 pb-2 bg-white text-sm text-gray-500 text-left ${index < sellTrades.length - 1 ? 'border-b border-gray-100' : ''}`}>
                                                        Sold {trade.quantity} {trade.ticker} @ ${trade.price.toFixed(2)}
                                                        <br></br>
                                                        Total: ${trade.totalValue.toFixed(2)}
                                                        <span className="text-xs text-gray-500 block text-right">{formatDate(trade.tradeDate)}</span>
                                                    </div>
                                                ))
                                            )}
                            </div>
                        </div>
                    </div>

                )}
            </div>
        </div>
    );
};

export { Sells };