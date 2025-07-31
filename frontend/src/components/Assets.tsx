import React from 'react';

const Asset = () => {
    const assets = [
        { name: 'Cash', percentage: 10.3, value: 285.25, color: 'text-purple-500' },
        { name: 'Equities', percentage: 89.7, value: 2474.33, color: 'text-blue-500' }
    ];

    const totalValue = 2759.58;

    // Calculate stroke-dasharray for donut chart
    const radius = 100;
    const circumference = 2 * Math.PI * radius;
    const cashOffset = 0;
    const equitiesOffset = (10.3 / 100) * circumference;

    return (
        
            <div className="bg-white rounded-2xl p-10 max-w-6xl mx-auto shadow-lg mb-10">
                <h2 className="text-2xl text-gray-500 mb-8 text-left">Asset Class Allocation</h2>

                <div className="flex flex-col lg:flex-row gap-20">
                    {/* Donut Chart */}
                    <div className="relative ml-40">
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

                            {/* Cash segment */}
                            <circle
                                cx="120"
                                cy="120"
                                r={radius}
                                fill="none"
                                stroke="#a855f7"
                                strokeWidth="24"
                                strokeDasharray={`${(10.3 / 100) * circumference} ${circumference}`}
                                strokeDashoffset={0}
                            />

                            {/* Equities segment */}
                            <circle
                                cx="120"
                                cy="120"
                                r={radius}
                                fill="none"
                                stroke="#3b82f6"
                                strokeWidth="24"
                                strokeDasharray={`${(89.7 / 100) * circumference} ${circumference}`}
                                strokeDashoffset={-equitiesOffset}
                            />
                        </svg>

                        {/* Center text */}
                        <div className="absolute inset-0 flex flex-col items-center justify-center">
                            <div className="text-2xl font-bold text-gray-800">
                                ${totalValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                            </div>
                        </div>
                    </div>

                    {/* Asset breakdown table */}
                    <div className="ml-12">
                        <div className="bg-white rounded-lg overflow-hidden shadow-sm">
                            <table className="min-w-full">
                                <thead>
                                    <tr className="border-b border-gray-200">
                                        <th className="px-6 py-4 text-left text-sm font-medium text-gray-500">Asset Class</th>
                                        <th className="px-6 py-4 text-right text-sm font-medium text-gray-500">Portfolio %</th>
                                        <th className="px-6 py-4 text-right text-sm font-medium text-gray-500">Market Value $</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr className="border-b border-gray-100 font-semibold">
                                        <td className="px-6 py-4 text-sm text-gray-900 text-left">Total Assets</td>
                                        <td className="px-6 py-4 text-sm text-gray-900 text-right">100.0%</td>
                                        <td className="px-6 py-4 text-sm text-gray-900 text-right">
                                            ${totalValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                                        </td>
                                    </tr>
                                    {assets.map((asset, index) => (
                                        <tr key={index} className="border-b border-gray-50">
                                            <td className={`px-6 py-4 text-sm font-medium text-left ${asset.color}`}>
                                                {asset.name}
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
        
    );
};

export { Asset };