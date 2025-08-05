import React from 'react';

const Equity = () => {
    const assets = [
        { name: 'AAPL', percentage: 15, value: 1500.25, color: '#a855f7' },
        { name: 'AMD', percentage: 20, value: 2000.33, color: '#3b82f6' },
        { name: 'NVDA', percentage: 25, value: 2500.33, color: '#f97316' },
        { name: 'FIG', percentage: 18, value: 1800.33, color: '#ec4899' },
        { name: 'TSLA', percentage: 12, value: 1200.50, color: '#10b981' },
        { name: 'MSFT', percentage: 7, value: 700.75, color: '#f59e0b' },
        { name: 'GOOGL', percentage: 3, value: 300.90, color: '#ef4444' }
    ];

    const totalValue = assets.reduce((sum, asset) => sum + asset.value, 0);

    // Calculate chart segments
    const radius = 100;
    const circumference = 2 * Math.PI * radius;
    
    let cumulativePercentage = 0;
    const chartSegments = assets.map(asset => {
        const segment = {
            ...asset,
            strokeDasharray: `${(asset.percentage / 100) * circumference} ${circumference}`,
            strokeDashoffset: -(cumulativePercentage / 100) * circumference
        };
        cumulativePercentage += asset.percentage;
        return segment;
    });

    return (
        <div className="bg-white rounded-2xl p-10 max-w-6xl mx-auto shadow-lg mb-10">
            <h2 className="text-2xl text-gray-500 mb-8 text-left">Equities Allocation</h2>

            <div className="flex flex-col lg:flex-row gap-20 items-start">
                {/* Donut Chart */}
                <div className="relative ml-40 mt-10">
                    <svg width="240" height="240" className="transform -rotate-90">
                        {/* Background circle */}
                        <circle
                            cx="120"
                            cy="120"
                            r={radius}
                            fill="none"
                            stroke="#e5e7eb"
                            strokeWidth="24"
                        />
                        
                        {/* Asset segments */}
                        {chartSegments.map((segment, index) => (
                            <circle
                                key={index}
                                cx="120"
                                cy="120"
                                r={radius}
                                fill="none"
                                stroke={segment.color}
                                strokeWidth="24"
                                strokeDasharray={segment.strokeDasharray}
                                strokeDashoffset={segment.strokeDashoffset}
                                
                                
                            />
                        ))}
                    </svg>

                    {/* Center text */}
                    <div className="absolute inset-0 flex flex-col items-center justify-center">
                        <div className="text-2xl font-bold text-gray-800">
                            ${totalValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </div>
                        <div className="text-sm text-gray-500">Total Equity Value</div>
                    </div>
                </div>

                {/* Asset breakdown table */}
                <div className="ml-12">
                    <div className="bg-white rounded-lg overflow-hidden shadow-sm border border-gray-200">
                        <div className="max-h-80 overflow-y-auto">
                            <table className="min-w-full">
                                <thead className="sticky top-0 bg-white z-10 border-b border-gray-200">
                                    <tr>
                                        <th className="px-6 py-4 text-left text-sm font-medium text-gray-500 bg-white">Symbol</th>
                                        <th className="px-6 py-4 text-right text-sm font-medium text-gray-500 bg-white">Portfolio %</th>
                                        <th className="px-6 py-4 text-right text-sm font-medium text-gray-500 bg-white">Market Value $</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {assets.map((asset, index) => (
                                        <tr key={index} className="border-b border-gray-50">
                                            <td className="px-6 py-4 text-sm font-medium text-left flex items-center">
                                                <div 
                                                    className="w-3 h-3 rounded-full mr-3" 
                                                    style={{ backgroundColor: asset.color }}
                                                ></div>
                                                <span>{asset.name}</span>
                                            </td>
                                            <td className="px-6 py-4 text-sm text-gray-700 text-right">
                                                {asset.percentage}%
                                            </td>
                                            <td className="px-6 py-4 text-sm text-gray-700 text-right">
                                                ${asset.value.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export {Equity};