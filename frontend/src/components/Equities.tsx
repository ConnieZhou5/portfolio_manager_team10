import React, { useState, useEffect } from 'react';
import { apiService, PortfolioItem } from '../services/api';
import { getTickerColor, clearColorMapping } from '../utils/colorMapping';

interface AssetData {
    name: string;
    percentage: number;
    value: number;
    color: string;
}

const Equity = () => {
    const [assets, setAssets] = useState<AssetData[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [totalValue, setTotalValue] = useState(0);

    useEffect(() => {
        // Clear color mapping to ensure fresh assignments
        clearColorMapping();
        fetchPortfolioData();
    }, []);

    const fetchPortfolioData = async () => {
        try {
            setLoading(true);
            setError(null);
            
            const portfolioItems = await apiService.getAllPortfolioItems();
            
            if (portfolioItems.length === 0) {
                setAssets([]);
                setTotalValue(0);
                setLoading(false);
                return;
            }

            // Calculate total portfolio value
            const total = portfolioItems.reduce((sum, item) => sum + item.totalValue, 0);
            setTotalValue(total);

            // Transform portfolio items to asset data format
            const assetData: AssetData[] = portfolioItems.map(item => {
                const percentage = total > 0 ? (item.totalValue / total) * 100 : 0;
                return {
                    name: item.ticker,
                    percentage: Math.round(percentage * 100) / 100, // Round to 2 decimal places
                    value: item.totalValue,
                    color: getTickerColor(item.ticker)
                };
            });

            // Sort by value (highest first)
            assetData.sort((a, b) => b.value - a.value);
            
            setAssets(assetData);
        } catch (err) {
            console.error('Error fetching portfolio data:', err);
            setError('Failed to load portfolio data. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

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

    if (loading) {
        return (
            <div className="bg-white rounded-2xl p-10 max-w-6xl mx-auto shadow-lg mb-10">
                <h2 className="text-2xl text-gray-500 mb-8 text-left">Equities Allocation</h2>
                <div className="flex items-center justify-center h-64">
                    <div className="text-gray-500">Loading portfolio data...</div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white rounded-2xl p-10 max-w-6xl mx-auto shadow-lg mb-10">
                <h2 className="text-2xl text-gray-500 mb-8 text-left">Equities Allocation</h2>
                <div className="flex items-center justify-center h-64">
                    <div className="text-red-500">{error}</div>
                </div>
            </div>
        );
    }

    if (assets.length === 0) {
        return (
            <div className="bg-white rounded-2xl p-10 max-w-6xl mx-auto shadow-lg mb-10">
                <h2 className="text-2xl text-gray-500 mb-8 text-left">Equities Allocation</h2>
                <div className="flex items-center justify-center h-64">
                    <div className="text-gray-500">No portfolio data available. Add some stocks to see your allocation.</div>
                </div>
            </div>
        );
    }

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