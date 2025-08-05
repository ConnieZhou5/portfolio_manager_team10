import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { apiService, TradeHistory, StockData, BuyRequest, CashBalance } from '../services/api';
import { usePortfolio } from '../context/PortfolioContext';
import { isMarketOpen } from '../utils/marketStatus';
import getAnalysis from './aiAnalysisService';
// import AIInsightsCard from "./AIInsightsCard"


type Market = 'Market Open' | 'Market Closed'

interface StockInfo {
    Symbol: string;
    Name: string;
    Price: string;
    DayGain: string;
    Open: string;
    PrevClose: string;
    Volume: string;
    DayRange: string;
    WeekRange52: string;
    MarketStatus: Market;
}

const Buys = () => {
    const [symbol, setSymbol] = useState('');
    const [quantity, setQuantity] = useState('');
    const [orderType, setOrderType] = useState('Market');
    const [cashBalance, setCashBalance] = useState<CashBalance | null>(null);
    const [invalidqty, setInvalidqty] = useState(false);
    const [buyTrades, setBuyTrades] = useState<TradeHistory[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [stockInfo, setStockInfo] = useState<StockInfo | null>(null);
    const [searchLoading, setSearchLoading] = useState(false);
    const [searchError, setSearchError] = useState<string | null>(null);
    const [searchTimeout, setSearchTimeout] = useState<NodeJS.Timeout | null>(null);
    const [buyLoading, setBuyLoading] = useState(false);
    const [buyError, setBuyError] = useState<string | null>(null);
    const [buySuccess, setBuySuccess] = useState<string | null>(null);
    const [marketOpen, setMarketOpen] = useState(true);
    const [isExpanded, setIsExpanded] = useState(true);
    const { triggerRefresh } = usePortfolio();
    const [analysis, setAnalysis] = useState<any>(null);

    // Auto-dismiss success and error messages after 10 seconds
    useEffect(() => {
        if (buySuccess) {
            const timer = setTimeout(() => {
                setBuySuccess(null);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [buySuccess]);

    useEffect(() => {
        if (buyError) {
            const timer = setTimeout(() => {
                setBuyError(null);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [buyError]);

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

    const fetchAnalysis = async (ticker: string) => {
        if (!ticker.trim()) return;
        try {
            const result = await getAnalysis(ticker.trim().toUpperCase());
            setAnalysis(result);
        } catch (err) {
            console.error('Error fetching analysis:', err);
            setAnalysis(null);
        }
    };


    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                // Fetch both buy trades and cash balance
                const [trades, cash] = await Promise.all([
                    apiService.getTradesByType('BUY'),
                    apiService.getCashBalance()
                ]);

                setBuyTrades(trades);
                setCashBalance(cash);
            } catch (err) {
                setError('Failed to load data');
                console.error('Error fetching data:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const searchStock = async (ticker: string) => {
        if (!ticker.trim()) {
            setStockInfo(null);
            setSearchError(null);
            return;
        }

        try {
            setSearchLoading(true);
            setSearchError(null);
            const stockData = await apiService.getStockData([ticker.toUpperCase()]);

            if (stockData.length > 0 && stockData[0].error) {
                setSearchError(`Error fetching data for ${ticker}: ${stockData[0].error}`);
                setStockInfo(null);
            } else if (stockData.length > 0) {
                const stock = stockData[0];
                const price = stock.price || 0;
                const previousClose = stock.previousClose || price * 0.98;
                const dayGain = stock.dayGain || (price - previousClose);
                const dayGainPercent = stock.dayGainPercent || ((dayGain / previousClose) * 100);
                const volume = stock.volume || 0;
                const dayLow = stock.dayLow || (price * 0.97); //placing a dummy buffer
                const dayHigh = stock.dayHigh || (price * 1.03);
                const yearLow = stock.yearLow || (price * 0.7);
                const yearHigh = stock.yearHigh || (price * 1.3);
                const marketStatus = stock.marketStatus || 'Market Open';

                setStockInfo({
                    Symbol: stock.symbol,
                    Name: stock.name || `${stock.symbol} Stock`,
                    Price: `$${price.toFixed(2)}`,
                    DayGain: `${dayGain >= 0 ? '+' : ''}${dayGain.toFixed(2)} (${dayGainPercent >= 0 ? '+' : ''}${dayGainPercent.toFixed(2)}%)`,
                    Open: `$${previousClose.toFixed(2)}`,
                    PrevClose: `$${previousClose.toFixed(2)}`,
                    Volume: volume > 0 ? `${(volume / 1000000).toFixed(0)}M` : 'N/A',
                    DayRange: `$${dayLow.toFixed(2)} - $${dayHigh.toFixed(2)}`,
                    WeekRange52: `$${yearLow.toFixed(2)} - $${yearHigh.toFixed(2)}`,
                    MarketStatus: marketStatus as Market
                });
                await fetchAnalysis(stock.symbol);
                setSearchError(null);
            } else {
                setSearchError(`No data found for ${ticker}`);
                setStockInfo(null);
            }
        } catch (err) {
            setSearchError(`Failed to fetch stock data for ${ticker}`);
            console.error('Error fetching stock data:', err);
            setStockInfo(null);
        } finally {
            setSearchLoading(false);
        }
    };

    const handleSymbolChange = (value: string) => {
        setSymbol(value);

        // Clear previous timeout
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }

        if (value.trim()) {
            // Set new timeout
            const timeoutId = setTimeout(() => {
                searchStock(value);
            }, 500); // Added 500ms debounce to prevent excessive API calls
            setSearchTimeout(timeoutId);
        } else {
            setStockInfo(null);
            setSearchError(null);
        }
    };

    const handleBuyTransaction = async () => {
        if (!stockInfo || !quantity || parseFloat(quantity) <= 0) {
            setBuyError('Please select a stock and enter a valid quantity');
            return;
        }

        if (!cashBalance) {
            setBuyError('Unable to verify cash balance');
            return;
        }

        const qty = parseInt(quantity);
        const price = parseFloat(stockInfo.Price.replace('$', ''));
        const totalCost = price * qty;

        if (totalCost > cashBalance.balance) {
            setBuyError(`Insufficient funds. Required: $${totalCost.toFixed(2)}, Available: $${cashBalance.balance.toFixed(2)}`);
            return;
        }

        try {
            setBuyLoading(true);
            setBuyError(null);
            setBuySuccess(null);

            const buyRequest: BuyRequest = {
                ticker: stockInfo.Symbol,
                quantity: qty,
                price: price,
                tradeDate: new Date().toISOString().split('T')[0] // Today's date in YYYY-MM-DD format
            };

            const response = await apiService.executeBuy(buyRequest);

            if (response.success) {
                setBuySuccess(`Successfully bought ${qty} shares of ${stockInfo.Symbol} for $${totalCost.toFixed(2)}`);
                setQuantity('');

                // Update cash balance with the remaining cash from the response
                if (response.remainingCash !== undefined) {
                    setCashBalance({
                        balance: response.remainingCash,
                        formattedBalance: `$${response.remainingCash.toFixed(2)}`
                    });
                }

                // Refresh buy trades
                const updatedTrades = await apiService.getTradesByType('BUY');
                setBuyTrades(updatedTrades);

                // Trigger portfolio refresh to update Stats and Assets components
                triggerRefresh();
            } else {
                setBuyError(response.error || 'Buy transaction failed');
            }
        } catch (err) {
            setBuyError('Failed to execute buy transaction');
            console.error('Error executing buy transaction:', err);
        } finally {
            setBuyLoading(false);
        }
    };

    const calculateTotal = () => {
        if (!stockInfo) return '0.00';
        const price = parseFloat(stockInfo.Price.replace('$', ''));
        const qty = parseFloat(quantity) || 0;
        return (price * qty).toFixed(2);
    };

    const exceedCash = () => {
        if (!cashBalance) return false;
        const total = parseFloat(calculateTotal());
        return total > cashBalance.balance && parseFloat(quantity) > 0;
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

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            month: 'numeric',
            day: 'numeric',
            year: 'numeric'
        });
    };

    return (
        <div className="bg-white rounded-3xl p-8 max-w-6xl mx-auto shadow-lg">
            {/* Header Tabs */}
            <div className="border-b border-gray-200 mb-8">
                <div className="flex space-x-8">
                    <Link to="/Positions">
                        <button className="text-purple-500 border-b-2 border-purple-500 pb-2 px-1 font-medium">
                            Buy
                        </button>
                    </Link>
                    <Link to="/Positions/Sell">
                        <button className="text-gray-500 hover:text-gray-700 pb-2 px-1 font-medium">
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
                            className="rounded-3xl block w-full pl-10 pr-3 py-3 rounded-lg bg-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                            placeholder="Search stocks..."
                            value={symbol}
                            onChange={(e) => handleSymbolChange(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                    const cleanSymbol = symbol.trim().toUpperCase();
                                    searchStock(cleanSymbol);
                                    fetchAnalysis(cleanSymbol);
                                }
                            }}

                        />
                        {searchLoading && (
                            <div className="absolute inset-y-0 right-0 pr-3 flex items-center">
                                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-purple-500"></div>
                            </div>
                        )}
                    </div>

                    {/* Stock Header */}
                    {stockInfo ? (
                        <div className="mb-6">
                            <div className="flex items-baseline justify-between">
                                <div className="flex items-baseline space-x-4">
                                    <h1 className="text-3xl font-bold text-gray-900">{stockInfo.Symbol}</h1>
                                    <span className="text-lg text-gray-600">{stockInfo.Name}</span>
                                </div>
                                <div className={`flex items-center space-x-2 px-3 py-1.5 rounded-full border ${getMarketStatusStyles().containerClass}`}>
                                    <div className={`w-2 h-2 rounded-full ${getMarketStatusStyles().dotClass}`}></div>
                                    <span className={`text-sm font-medium ${getMarketStatusStyles().textClass}`}>
                                        {stockInfo.MarketStatus}
                                    </span>
                                </div>
                            </div>
                            <div className="flex items-baseline space-x-4">
                                <span className="text-3xl font-bold text-gray-900">{stockInfo.Price}</span>
                                <span className={`text-lg font-medium ${stockInfo.DayGain.includes('+') ? 'text-green-600' : 'text-red-600'}`}>
                                    {stockInfo.DayGain}
                                </span>
                            </div>
                        </div>
                    ) : searchError ? (
                        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                            <p className="text-red-800">{searchError}</p>
                        </div>
                    ) : (
                        <div className="mb-6 space-y-4">
                            <div className={`flex justify-end mb-4`}>
                                <div className={`flex items-center space-x-2 px-3 py-1.5 rounded-full border ${getMarketStatusStyles().containerClass}`}>
                                    <div className={`w-2 h-2 rounded-full ${getMarketStatusStyles().dotClass}`}></div>
                                    <span className={`text-sm font-medium ${getMarketStatusStyles().textClass}`}>
                                        {marketOpen ? 'Market Open' : 'Market Closed'}
                                    </span>
                                </div>
                            </div>

                            <div className="p-4 bg-gray-50 border border-gray-200 rounded-lg">
                                <p className="text-gray-600">Enter a stock symbol to search for real-time data</p>
                            </div>
                        </div>
                    )}

                    {/* Stock Details */}
                    <div className="relative">
                        {stockInfo && (
                            <div className="grid grid-cols-2 gap-8 mb-8">
                                <div className="space-y-4">
                                    <div className="flex justify-between shadow-sm">
                                        <span className="text-gray-600">Open</span>
                                        <span className="font-medium text-gray-900">{stockInfo.Open}</span>
                                    </div>
                                    <div className="flex justify-between shadow-sm">
                                        <span className="text-gray-600">Previous Close</span>
                                        <span className="font-medium text-gray-900">{stockInfo.PrevClose}</span>
                                    </div>
                                    <div className="flex justify-between shadow-sm">
                                        <span className="text-gray-600">Volume</span>
                                        <span className="font-medium text-gray-900">{stockInfo.Volume}</span>
                                    </div>
                                </div>

                                <div className="space-y-4">
                                    <div className="flex justify-between shadow-sm">
                                        <span className="text-gray-600">Day Range</span>
                                        <span className="font-medium text-gray-900">{stockInfo.DayRange}</span>
                                    </div>
                                    <div className="flex justify-between shadow-sm">
                                        <span className="text-gray-600">52 Week Range</span>
                                        <span className="font-medium text-gray-900">{stockInfo.WeekRange52}</span>
                                    </div>
                                </div>
                            </div>
                        )}

                        <div className="absolute top-24 -right-7 z-10">
                            {isExpanded ? (
                                <button
                                    onClick={() => setIsExpanded(false)}
                                    className="bg-purple-500 hover:bg-purple-600 text-white p-2 rounded-full shadow-lg transition-colors"
                                    aria-label="Close trading panel"
                                >
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                    </svg>
                                </button>
                            ) : (
                                <button
                                    onClick={() => setIsExpanded(true)}
                                    className="bg-purple-500 hover:bg-purple-600 text-white p-2 rounded-full shadow-lg transition-colors"
                                    aria-label="Open trading panel"
                                >
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                                    </svg>
                                </button>
                            )}
                        </div>

                        {/* Chart Placeholder */}
                        <div className="w-full h-[500px] bg-gray-50 rounded-lg flex items-center justify-center">
                            {/* This div will be replaced with the AI analysis */}
                            {analysis ? (
                                <div className="ai-insights">
                                    <div className="recommendation">
                                        <h3>Recommendation: {analysis.generalAnalysis}</h3>
                                        <div>Analyst Rating: Positive</div>
                                        <div>News: Mixed</div>
                                        <div>Social Media: Avoid</div>
                                        <div>Technical Analysis: Bullish</div>
                                    </div>
                                </div>
                            ) : (
                                <p>Loading analysis...</p>
                            )}
                        </div>
                    </div>
                </div>

                {/* Right Column - Buy Form and Transactions */}
                {isExpanded && (
                    <div className="space-y-6 animate-in slide-in-from-right duration-600 bg-white border-l border-gray-200 pl-6 -mr-8 pr-8">
                        {/* Buy Form */}
                        <div className="bg-white border border-gray-100 rounded-3xl p-8 space-y-6 shadow-sm">
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-lg font-semibold text-gray-800">Buy Order</h3>
                            </div>
                            {/* Symbol Input */}
                            <div className="flex">
                                <label className="block text-md text-gray-700 mt-2">Symbol</label>
                                <div className="ml-[59px] w-full px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 font-medium rounded-3xl">
                                    {stockInfo ? stockInfo.Symbol : 'Symbol'}
                                </div>
                            </div>

                            {/* Quantity Input */}
                            <div className="flex">
                                <label className="block text-md text-gray-700 mt-2">Quantity</label>
                                <input
                                    type="number"
                                    className="rounded-3xl text-center ml-[51px] w-full px-3 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                                    value={quantity}
                                    onChange={(e) => {
                                        const value = e.target.value;
                                        if (value === '' || (Number(value) >= 0 && Number.isInteger(Number(value)))) {
                                            setQuantity(value);
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
                            {buyError && (
                                <div className="bg-red-100 text-red-700 text-sm p-2 rounded-lg">
                                    ❌ {buyError}
                                </div>
                            )}

                            {buySuccess && (
                                <div className="bg-green-100 text-green-700 text-sm p-2 rounded-lg">
                                    ✅ {buySuccess}
                                </div>
                            )}

                            {/* Market Status Indicator */}
                            {!marketOpen && (
                                <div className="bg-red-50 border border-red-200 rounded-lg p-2 text-center">
                                    <span className="text-red-700 text-sm font-medium">Market Closed - Cannot Buy</span>
                                </div>
                            )}

                            {/* Action Buttons */}
                            <div className="flex space-x-5 pt-4">
                                <button
                                    className={`rounded-3xl flex-1 font-medium py-3 px-6 transition-colors ${!stockInfo || parseFloat(quantity) <= 0 || buyLoading || !marketOpen
                                        ? 'bg-gray-400 text-gray-600 cursor-not-allowed'
                                        : 'bg-purple-500 hover:bg-purple-600 text-white'
                                        } ${!marketOpen ? 'hover:cursor-not-allowed' : ''}`}
                                    disabled={!stockInfo || parseFloat(quantity) <= 0 || buyLoading || !marketOpen}
                                    onClick={handleBuyTransaction}
                                >
                                    {buyLoading ? 'Processing...' : 'Buy'}
                                </button>
                                <button className="rounded-3xl flex-1 border border-gray-300 bg-white hover:bg-gray-200 text-gray-700 font-medium py-3 px-6 transition-colors"
                                    onClick={() => {
                                        setQuantity('');
                                        setInvalidqty(false);
                                        setBuyError(null);
                                        setBuySuccess(null);
                                    }}>
                                    Cancel
                                </button>
                            </div>

                        </div>

                        {/* Buy Transactions Log */}
                        <div className="rounded-3xl bg-white border border-gray-100 overflow-hidden shadow-sm">
                            <div className="flex items-center justify-between px-6 pt-6 pb-2 mb-1 border-b border-gray-100 ">
                                <h3 className="text-gray-600 font-medium">Buy Transactions Log</h3>


                            </div>
                            <div className="max-h-96 overflow-y-auto">
                                {loading ? (
                                    <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left">
                                        Loading buy transactions...
                                    </div>
                                ) : error ? (
                                    <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-red-500 text-left">
                                        {error}
                                    </div>
                                ) : buyTrades.length === 0 ? (
                                    <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left">
                                        No buy transactions found
                                    </div>
                                ) : (
                                    buyTrades.map((trade, index) => (
                                        <div key={trade.id} className={`pl-6 pr-6 pt-2 pb-2 bg-white text-sm text-gray-500 text-left ${index < buyTrades.length - 1 ? 'border-b border-gray-100' : ''}`}>
                                            Bought {trade.quantity} {trade.ticker} @ ${trade.price.toFixed(2)}
                                            <br />
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

export { Buys };