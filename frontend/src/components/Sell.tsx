import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import PivotTable from './PivotTable';


type Market = 'Market Open' | 'Market Closed'

const stockData = {
    MarketStatus: 'Market Open' //need to calculate with time
};

// List of data for the table (you can modify this to pull data from an API or elsewhere)
const portfolioData = [
    { symbol: 'AAPL', lastPrice: 190.5, change: -1.2, changePercent: -0.63, quantity: 10, pricePaid: 185, daysGain: -12, totalGain: 55, totalGainPercent: 30, value: 1905, date: '08/01/25' },
    { symbol: 'AAPL', lastPrice: 191.0, change: -0.5, changePercent: -0.26, quantity: 20, pricePaid: 180, daysGain: -10, totalGain: 75, totalGainPercent: 41.6, value: 3820, date: '07/14/25' },
    { symbol: 'GOOG', lastPrice: 130.0, change: 2.0, changePercent: 1.56, quantity: 5, pricePaid: 125, daysGain: 10, totalGain: 25, totalGainPercent: 20, value: 650, date: '07/31/25' }
];

const Sells = () => {
    const [symbol, setSymbol] = useState('');
    const [quantity, setQuantity] = useState('');
    const [orderType, setOrderType] = useState('Market');
    // const [quantity, setQuantity] = useState();
    const isQuantityInvalid = parseFloat(quantity) <= 0 || isNaN(parseFloat(quantity));
    const [invalidqty, setQtyError] = useState(false);
    const [invalidstock, setStockError] = useState(false);



    const getMarketStatusStyles = () => {
        if (stockData.MarketStatus === 'Market Open') {
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


    const [expandedRows, setExpandedRows] = useState<{ [key: string]: boolean }>({});
    const [searchQuery, setSearchQuery] = useState<string>(''); // State for search query

    const toggleRow = (symbol: string) => {
        setExpandedRows(prev => ({ ...prev, [symbol]: !prev[symbol] }));
    };




    // Filter data based on the search query
    const filteredData = portfolioData.filter(row => row.symbol.toLowerCase().includes(searchQuery.toLowerCase()));

    return (
        <div className="bg-white rounded-2xl p-8 max-w-6xl mx-auto shadow-lg">
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
                            className="block w-full pl-10 pr-3 py-3 rounded-lg bg-gray-100 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                            placeholder="Search stocks..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                    </div>

                    <div className={`ml-[587px] mb-2 flex items-center space-x-2 px-3 py-1.5 rounded-full border ${getMarketStatusStyles().containerClass}`}>
                        <div className={`w-2 h-2 rounded-full ${getMarketStatusStyles().dotClass}`}></div>
                        <span className={`text-sm font-medium ${getMarketStatusStyles().textClass}`}>
                            {stockData.MarketStatus}
                        </span>
                    </div>

                    {/* Table Section */}
                    <div className="col-span-2 bg-white rounded-2xl p-6 shadow-md text-xs">
                        <PivotTable data={portfolioData} searchText={searchQuery} />
                    </div>

                </div>

                {/* Right Column - Sell Form and Transactions (Sticky) */}
                <div className="space-y-6">
                    {/* Sell Form */}

                    <div className="bg-orange-50 rounded-2xl p-10 space-y-6">

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

                        {/* Symbol Input */}
                        <div className="flex">
                            <label className="block text-md text-gray-700 mt-2">Symbol</label>
                            <input
                                type="text"
                                className="text-center ml-[59px] w-full px-3 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                                value={symbol}


                                onChange={(e) => {
                                    const value = e.target.value;
                                    const stock = portfolioData.find(item => item.symbol === symbol);

                                    const maxQty = stock ? stock.quantity : 0;

                                    if (maxQty >= 0) {
                                        setSymbol(value);
                                        setStockError(false);
                                    }
                                }}
                            />
                        </div>

                        {/* Quantity Input */}
                        <div className="flex">
                            <label className="block text-md text-gray-700 mt-2">Quantity</label>
                            <input
                                type="number"
                                className="text-center ml-[51px] w-full px-3 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
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
                                className="flex-1 bg-orange-500 hover:bg-orange-600 text-white font-medium py-3 px-6 rounded-lg transition-colors"
                                onClick={() => {
                                    const stock = portfolioData.find(item => item.symbol === symbol);
                                    const maxQty = stock ? stock.quantity : 0;

                                    if (maxQty == 0) {
                                        setStockError(true);
                                    }
                                    else if (parseFloat(quantity) > maxQty || isQuantityInvalid) {
                                        setQtyError(true);
                                    } else {
                                        // Process sell order here
                                        console.log('Sell order processed');
                                    }
                                }}
                            >
                                Sell
                            </button>
                            <button className="flex-1 border border-gray-300 bg-white hover:bg-gray-200 text-gray-700 font-medium py-3 px-6 rounded-lg transition-colors"
                                onClick={() => {
                                    setQuantity('');
                                    setSymbol('');
                                    setQtyError(false);
                                    setStockError(false);
                                }}>
                                Cancel
                            </button>
                        </div>
                    </div>

                    {/* Sell Transactions Log */}
                    <div className="bg-gray-100 rounded-lg overflow-hidden shadow-sm">
                        <h3 className="text-gray-600 pt-6 pb-2 pl-6 mb-1 font-medium text-left">Sell Transactions Log</h3>
                        <div className="max-h-96 overflow-y-auto">
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Sold 5 AAPL @ $188.98
                                <br></br>
                                Total: $944.90
                                <span className="text-sm text-gray-500 block text-right">7/10/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Sold 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Sold 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Sold 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Sold 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left">
                                Sold 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export { Sells };