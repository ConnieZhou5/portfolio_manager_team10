import React, { useState } from 'react';
import { Link } from 'react-router-dom';


type Market = 'Market Open' | 'Market Closed'

const stockData = {
    MarketStatus: 'Market Open' //need to calculate with time
};

// List of data for the table (you can modify this to pull data from an API or elsewhere)
const tableData = [
    { symbol: 'AMD', lastPrice: 12.00, changeDollar: 12.00, changePercent: 12.00, qty: 12, pricePaid: 12.00, dayGain: 12.00, totalGain: 12.00, totalGainPercent: 12.00, value: 12.00, date: '07/09/2025' },
    { symbol: 'AMZ', lastPrice: 34.00, changeDollar: 34.00, changePercent: 34.00, qty: 1, pricePaid: 34.00, dayGain: 34.00, totalGain: 34.00, totalGainPercent: 34.00, value: 34.00, date: '06/08/2025' },
    { symbol: 'ARKK', lastPrice: 44.00, changeDollar: 44.00, changePercent: 44.00, qty: 6, pricePaid: 44.00, dayGain: 44.00, totalGain: 44.00, totalGainPercent: 44.00, value: 44.00, date: '06/08/2025' }
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
        const stock = tableData.find(item => item.symbol === symbol);
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
    const filteredData = tableData.filter(row => row.symbol.toLowerCase().includes(searchQuery.toLowerCase()));

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
                        <table className="w-full text-xs text-left">
                            <thead className="text-gray-700 border-b align-center h-8">
                                <tr>
                                    <th>Symbol</th>
                                    <th>Last Price $</th>
                                    <th>Change $</th>
                                    <th>Change %</th>
                                    <th>Qty #</th>
                                    <th>Price Paid $</th>
                                    <th>Day's Gain $</th>
                                    <th>Total Gain $</th>
                                    <th>Total Gain %</th>
                                    <th>Value $</th>
                                </tr>
                            </thead>
                            <tbody>
                                {/* Filtered Data */}
                                {filteredData.map((row) => (
                                    <React.Fragment key={row.symbol}>
                                        <tr className="border-b cursor-pointer text-center align-center h-8" onClick={() => toggleRow(row.symbol)}>
                                            <td>
                                                <span className="text-black-200 hover:underline">{expandedRows[row.symbol] ? '▼' : '▶'} {row.symbol}</span>
                                            </td>
                                            <td>{row.lastPrice.toFixed(2)}</td>
                                            <td>{row.changeDollar.toFixed(2)}</td>
                                            <td>{row.changePercent.toFixed(2)}</td>
                                            <td>{row.qty}</td>
                                            <td>{row.pricePaid.toFixed(2)}</td>
                                            <td>{row.dayGain.toFixed(2)}</td>
                                            <td>{row.totalGain.toFixed(2)}</td>
                                            <td>{row.totalGainPercent.toFixed(2)}</td>
                                            <td>{row.value.toFixed(2)}</td>
                                        </tr>
                                        {expandedRows[row.symbol] && (
                                            <tr className="bg-gray-100 text-gray-500 text-center align-center h-8">
                                                <td>{row.date}</td>
                                                <td>{row.lastPrice.toFixed(2)}</td>
                                                <td>{row.changeDollar.toFixed(2)}</td>
                                                <td>{row.changePercent.toFixed(2)}</td>
                                                <td>{row.qty}</td>
                                                <td>{row.pricePaid.toFixed(2)}</td>
                                                <td>{row.dayGain.toFixed(2)}</td>
                                                <td>{row.totalGain.toFixed(2)}</td>
                                                <td>{row.totalGainPercent.toFixed(2)}</td>
                                                <td>{row.value.toFixed(2)}</td>
                                            </tr>
                                        )}
                                    </React.Fragment>
                                ))}
                                {/* Total Row... */}
                                <tr className="font-semibold border-t h-8">
                                    <td colSpan={5} className="text-left pl-4">Total</td>
                                    <td className="text-center align-center">2,241.84</td>
                                    <td className="text-center align-center">12.88</td>
                                    <td className="text-center align-center">232.44</td>
                                    <td className="text-center align-center">10.37</td>
                                    <td className="text-center align-center">2,474.33</td>
                                </tr>
                            </tbody>
                        </table>
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
                                    const stock = tableData.find(item => item.symbol === symbol);

                                    const maxQty = stock ? stock.qty : 0;

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
                                    const stock = tableData.find(item => item.symbol === symbol);
                                    const maxQty = stock ? stock.qty : 0;

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