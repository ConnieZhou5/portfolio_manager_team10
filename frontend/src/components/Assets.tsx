import React, { useState, useEffect } from 'react';
import { apiService, PortfolioStats, CashBalance } from '../services/api';

interface Asset {
    name: string;
    percentage: number;
    value: number;
    color: string;
}

const Asset = () => {
    const [assets, setAssets] = useState<Asset[]>([]);
    const [totalValue, setTotalValue] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchAssetData = async () => {
            try {
                setLoading(true);
                setError(null);

                // Fetch portfolio stats and cash balance
                const [portfolioStats, cashBalance] = await Promise.all([
                    apiService.getPortfolioStats(),
                    apiService.getCashBalance()
                ]);

                // Parse the currency strings to get numeric values
                const cashValue = parseFloat(portfolioStats.cash.replace('$', '').replace(',', ''));
                const investmentsValue = parseFloat(portfolioStats.investments.replace('$', '').replace(',', ''));
                const totalAssetsValue = parseFloat(portfolioStats.totalAssets.replace('$', '').replace(',', ''));

                // Calculate percentages
                const cashPercentage = totalAssetsValue > 0 ? (cashValue / totalAssetsValue) * 100 : 0;
                const equitiesPercentage = totalAssetsValue > 0 ? (investmentsValue / totalAssetsValue) * 100 : 0;

                const assetData: Asset[] = [
                    { 
                        name: 'Cash', 
                        percentage: Math.round(cashPercentage * 10) / 10, 
                        value: cashValue, 
                        color: 'text-purple-500' 
                    },
                    { 
                        name: 'Equities', 
                        percentage: Math.round(equitiesPercentage * 10) / 10, 
                        value: investmentsValue, 
                        color: 'text-blue-500' 
                    }
                ];

                setAssets(assetData);
                setTotalValue(totalAssetsValue);
            } catch (err) {
                setError('Failed to load asset data');
                console.error('Error fetching asset data:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchAssetData();
    }, []);

    // Calculate stroke-dasharray for donut chart
    const radius = 100;
    const circumference = 2 * Math.PI * radius;
    const cashOffset = 0;
    const equitiesOffset = assets.length > 0 ? (assets[0].percentage / 100) * circumference : 0;

    if (loading) {
        return (
            <div className="bg-white rounded-2xl p-10 max-w-6xl mx-auto shadow-lg mb-10">
                <h2 className="text-2xl text-gray-500 mb-8 text-left">Asset Class Allocation</h2>
                <div className="flex items-center justify-center h-64">
                    <div className="text-gray-500">Loading asset data...</div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white rounded-2xl p-10 max-w-6xl mx-auto shadow-lg mb-10">
                <h2 className="text-2xl text-gray-500 mb-8 text-left">Asset Class Allocation</h2>
                <div className="flex items-center justify-center h-64">
                    <div className="text-red-500">{error}</div>
                </div>
            </div>
        );
    }

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
                        {assets.length > 0 && (
                            <circle
                                cx="120"
                                cy="120"
                                r={radius}
                                fill="none"
                                stroke="#a855f7"
                                strokeWidth="24"
                                strokeDasharray={`${(assets[0].percentage / 100) * circumference} ${circumference}`}
                                strokeDashoffset={0}
                            />
                        )}

                        {/* Equities segment */}
                        {assets.length > 1 && (
                            <circle
                                cx="120"
                                cy="120"
                                r={radius}
                                fill="none"
                                stroke="#3b82f6"
                                strokeWidth="24"
                                strokeDasharray={`${(assets[1].percentage / 100) * circumference} ${circumference}`}
                                strokeDashoffset={-equitiesOffset}
                            />
                        )}
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