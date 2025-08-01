import React, { useState } from 'react';
import { Link } from 'react-router-dom';


type Market = 'Market Open' | 'Market Closed'

const stockData = {
    Symbol: 'AMD',
    Name: 'ADVANCED MICRO DEVICES INC COM',
    Price: '$173.66', //reg market price
    DayGain: '+7.19 (+4.32%)', //reg market price - chart prev close
    Open: '$208.27', //open
    PrevClose: '$209.05', // chartPreviousClose
    Volume: '39,000,000', // regularMarketVolume
    DayRange: '$207.00 - $209.10', // regularMarketDayLow, regularMarketDayHigh
    WeekRange52: '$76.48 - $209.10', // fiftyTwoWeekLow, fiftyTwoWeekHigh
    MarketStatus: 'Market Open' //need to calculate with time
};

const Buys = () => {
    const [symbol, setSymbol] = useState(stockData.Symbol);
    const [quantity, setQuantity] = useState('');
    const [orderType, setOrderType] = useState('Market');
    const [cashOnHand, setCashOnHand] = useState(500); // Default cash value
    const [invalidqty, setInvalidqty] = useState(false);


    const calculateTotal = () => {
        const price = parseFloat(stockData.Price.replace('$', ''));
        const qty = parseFloat(quantity) || 0;
        return (price * qty).toFixed(2);
    };

    const exceedCash = () => {
        const total = parseFloat(calculateTotal());
        return total > cashOnHand && parseFloat(quantity) > 0;
    };

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
                            onChange={(e) => setSymbol(e.target.value)}
                        />
                    </div>

                    {/* Stock Header */}
                    <div className="mb-6">
                        <div className="flex items-baseline justify-between">
                            <div className="flex items-baseline space-x-4">
                                <h1 className="text-3xl font-bold text-gray-900">{stockData.Symbol}</h1>
                                <span className="text-lg text-gray-600">{stockData.Name}</span>
                            </div>
                            <div className={`flex items-center space-x-2 px-3 py-1.5 rounded-full border ${getMarketStatusStyles().containerClass}`}>
                                <div className={`w-2 h-2 rounded-full ${getMarketStatusStyles().dotClass}`}></div>
                                <span className={`text-sm font-medium ${getMarketStatusStyles().textClass}`}>
                                    {stockData.MarketStatus}
                                </span>
                            </div>
                        </div>
                        <div className="flex items-baseline space-x-4">
                            <span className="text-3xl font-bold text-gray-900">{stockData.Price}</span>
                            <span className="text-lg text-green-600 font-medium">{stockData.DayGain}</span>
                        </div>
                    </div>

                    {/* Stock Details */}
                    <div className="space-y-4">
                        <div className="flex justify-between shadow-sm">
                            <span className="text-gray-600">Open</span>
                            <span className="font-medium text-gray-900">{stockData.Open}</span>
                        </div>
                        <div className="flex justify-between shadow-sm">
                            <span className="text-gray-600">Previous Close</span>
                            <span className="font-medium text-gray-900">{stockData.PrevClose}</span>
                        </div>
                        <div className="flex justify-between shadow-sm">
                            <span className="text-gray-600">Volume</span>
                            <span className="font-medium text-gray-900">{stockData.Volume}</span>
                        </div>
                        <div className="flex justify-between shadow-sm">
                            <span className="text-gray-600">Day Range</span>
                            <span className="font-medium text-gray-900">{stockData.DayRange}</span>
                        </div>
                        <div className="flex justify-between shadow-sm">
                            <span className="text-gray-600">52 Week Range</span>
                            <span className="font-medium text-gray-900">{stockData.WeekRange52}</span>
                        </div>
                    </div>

                    {/* placeholder */}
                    <div className="mt-12 space-y-4">
                        <div className="h-[500px] bg-gray-50 rounded-lg flex items-center justify-center">
                            <span className="text-gray-400">placeholder</span>
                        </div>
                    </div>
                </div>

                {/* Right Column - Buy Form and Transactions (Sticky) */}
                <div className="space-y-6">
                    {/* Buy Form */}
                    <div className="bg-purple-50 rounded-2xl p-10 space-y-6">

                        {invalidqty && (
                            <div className="bg-red-100 text-red-700 text-sm p-2 rounded-lg">
                                ❌ Not Valid Quantity
                            </div>
                        )}

                        {/* Symbol Input */}
                        <div className="flex">
                            <label className="block text-md text-gray-700 mt-2">Symbol</label>
                            <div className="ml-[59px] w-full px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 font-medium">
                                {stockData.Symbol}
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
                            <button className="flex-1 bg-purple-500 hover:bg-purple-600 text-white font-medium py-3 px-6 rounded-lg transition-colors"
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
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Bought 5 AAPL @ $188.98
                                <br></br>
                                Total: $944.90
                                <span className="text-sm text-gray-500 block text-right">7/10/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Bought 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Bought 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Bought 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left border-b border-gray-100">
                                Bought 1 AAPL @ $188.98
                                <br></br>
                                Total: $188.98
                                <span className="text-sm text-gray-500 block text-right">7/9/2025</span>
                            </div>
                            <div className="pl-6 pr-6 pt-2 pb-2 bg-white text-md text-gray-500 text-left">
                                Bought 1 AAPL @ $188.98
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

export { Buys };