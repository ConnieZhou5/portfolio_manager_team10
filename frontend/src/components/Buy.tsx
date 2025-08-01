import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { apiService, TradeHistory, StockData } from '../services/api';

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
    const [cashOnHand, setCashOnHand] = useState(500); // Default cash value
    const [invalidqty, setInvalidqty] = useState(false);
    const [buyTrades, setBuyTrades] = useState<TradeHistory[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [stockInfo, setStockInfo] = useState<StockInfo | null>(null);
    const [searchLoading, setSearchLoading] = useState(false);
    const [searchError, setSearchError] = useState<string | null>(null);
    const [searchTimeout, setSearchTimeout] = useState<NodeJS.Timeout | null>(null);

    useEffect(() => {
        const fetchBuyTrades = async () => {
            try {
                setLoading(true);
                setError(null);
                const trades = await apiService.getTradesByType('BUY');
                setBuyTrades(trades);
            } catch (err) {
                setError('Failed to load buy transactions');
                console.error('Error fetching buy trades:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchBuyTrades();
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
            }, 500);
            setSearchTimeout(timeoutId);
        } else {
            setStockInfo(null);
            setSearchError(null);
        }
    };

    const calculateTotal = () => {
        if (!stockInfo) return '0.00';
        const price = parseFloat(stockInfo.Price.replace('$', ''));
        const qty = parseFloat(quantity) || 0;
        return (price * qty).toFixed(2);
    };

    const exceedCash = () => {
        const total = parseFloat(calculateTotal());
        return total > cashOnHand && parseFloat(quantity) > 0;
    };

    const getMarketStatusStyles = () => {
        if (!stockInfo) {
            return {
                containerClass: 'bg-gray-50 border-gray-200',
                dotClass: 'bg-gray-500',
                textClass: 'text-gray-700'
            };
        }
        
        if (stockInfo.MarketStatus === 'Market Open') {
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
        <div className="bg-white rounded-2xl p-8 max-w-6xl mx-auto shadow-lg">
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

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Left Column - Stock Info */}
                <div className="lg:col-span-2">
                    {/* Search Bar */}
                    <div className="relative mb-6">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center">
                            <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                            </svg>
                        </div>
                        <input
                            type="text"
                            className="block w-full pl-10 pr-3 py-3 rounded-lg bg-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                            placeholder="Search stocks..."
                            value={symbol}
                            onChange={(e) => handleSymbolChange(e.target.value)}
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
                        <div className="mb-6 p-4 bg-gray-50 border border-gray-200 rounded-lg">
                            <p className="text-gray-600">Enter a stock symbol to search for real-time data</p>
                        </div>
                    )}

                    {/* Stock Details */}
                    {stockInfo && (
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
                            <div className="flex justify-between shadow-sm">
                                <span className="text-gray-600">Day Range</span>
                                <span className="font-medium text-gray-900">{stockInfo.DayRange}</span>
                            </div>
                            <div className="flex justify-between shadow-sm">
                                <span className="text-gray-600">52 Week Range</span>
                                <span className="font-medium text-gray-900">{stockInfo.WeekRange52}</span>
                            </div>
                        </div>
                    )}

                    {/* placeholder */}
                    <div className="mt-12 space-y-4">
                        <div className="h-[500px] bg-gray-50 rounded-lg flex items-center justify-center">
                            <span className="text-gray-400">Innovation brewing. Stay tuned.</span>
                        </div>
                    </div>
                </div>

                {/* Right Column - Buy Form and Transactions (Sticky) */}
                <div className="space-y-6">
                    {/* Buy Form */}
                    <div className="bg-purple-50 rounded-2xl p-10 space-y-6">
                        {/* Symbol Input */}
                        <div className="flex">
                            <label className="block text-md text-gray-700 mt-2">Symbol</label>
                            <div className="ml-[59px] w-full px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 font-medium">
                                {stockInfo ? stockInfo.Symbol : 'Enter symbol'}
                            </div>
                        </div>

                        {/* Quantity Input */}
                        <div className="flex">
                            <label className="block text-md text-gray-700 mt-2">Quantity</label>
                            <input
                                type="number"
                                className="text-center ml-[51px] w-full px-3 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
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
                            <div className="ml-[70px] w-full px-3 pr-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 font-medium">
                                {orderType}
                            </div>
                        </div>

                        {/* Total */}
                        <div className="flex">
                            <label className="block text-md text-gray-700 mt-2">Total~</label>
                            <div className="ml-[66px] w-full px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 font-medium">
                                ${calculateTotal()}
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div className="flex space-x-5 pt-4">
                            <button 
                                className="flex-1 bg-purple-500 hover:bg-purple-600 text-white font-medium py-3 px-6 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                disabled={!stockInfo || parseFloat(quantity) <= 0}
                                onClick={() => {
                                    const total = parseFloat(calculateTotal());
                                    const qty = parseFloat(quantity) || 0;

                                    if (qty <= 0 || !Number.isInteger(qty)) {
                                        setInvalidqty(true);
                                        return;
                                    }

                                    if (total > cashOnHand) {
                                        setInvalidqty(true);
                                        return;
                                    } 
                                    // If validation passes
                                    setInvalidqty(false);
                                    
                                    setQuantity('');

                                }}> 
                                Buy
                            </button>
                            <button className="flex-1 border border-gray-300 bg-white hover:bg-gray-200 text-gray-700 font-medium py-3 px-6 rounded-lg transition-colors"
                                onClick={() => {
                                    setQuantity('');
                                    setInvalidqty(false);
                                }}>
                                Cancel
                            </button>
                        </div>
                    </div>

                    {/* Buy Transactions Log */}
                    <div className="bg-gray-100 rounded-lg overflow-hidden shadow-sm">
                        <h3 className="text-gray-600 pt-6 pb-2 pl-6 mb-1 font-medium text-left">Buy Transactions Log</h3>
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
                                    <div key={trade.id} className={`pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left ${index < buyTrades.length - 1 ? 'border-b border-gray-100' : ''}`}>
                                        Bought {trade.quantity} {trade.ticker} @ ${trade.price.toFixed(2)}
                                        <br></br>
                                        Total: ${trade.totalValue.toFixed(2)}
                                        <span className="text-sm text-gray-500 block text-right">{formatDate(trade.tradeDate)}</span>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export { Buys };