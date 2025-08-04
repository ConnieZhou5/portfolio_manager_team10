import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, ResponsiveContainer, Tooltip } from 'recharts';
import { apiService, PnLResponse, MonthlyPnLData } from '../services/api';

// Custom tooltip component
const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
        return (
            <div className="bg-white p-3 border border-gray-200 rounded-lg shadow-lg">
                <p className="text-gray-600 font-medium mb-2">{label}</p>
                {payload.map((entry: any, index: number) => (
                    <p key={index} className="text-sm" style={{ color: entry.color }}>
                        {entry.name}: ${entry.value.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </p>
                ))}
            </div>
        );
    }
    return null;
};

export default function Performance() {
    const [pnLData, setPnLData] = useState<PnLResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchPnLData = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await apiService.getMonthlyPnL();
                setPnLData(data);
            } catch (err) {
                setError('Failed to load P&L data. Check if backend is running.');
                console.error('Error fetching P&L data:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchPnLData();
    }, []);

    // Calculate Y-axis domain with clean intervals
    const calculateYAxisDomain = (data: MonthlyPnLData[]) => {
        if (!data || data.length === 0) return [0, 1000];
        
        const allValues = data.flatMap(item => [item.realized, item.unrealized]);
        const minValue = Math.min(...allValues);
        const maxValue = Math.max(...allValues);
        
        // Add some padding to the range
        const range = maxValue - minValue;
        const padding = range * 0.1;
        
        const domainMin = Math.floor((minValue - padding) / 100) * 100;
        const domainMax = Math.ceil((maxValue + padding) / 100) * 100;
        
        return [domainMin, domainMax];
    };

    if (loading) {
        return (
            <div className="bg-white rounded-3xl p-10 max-w-6xl mx-auto shadow-lg mb-20">
                <div className="mb-8">
                    <h1 className="text-2xl text-gray-500 mb-8 text-left">Profit and Loss</h1>
                </div>
                <div className="flex items-center justify-center h-80">
                    <div className="text-gray-500">Loading P&L data...</div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white rounded-3xl p-10 max-w-6xl mx-auto shadow-lg mb-20">
                <div className="mb-8">
                    <h1 className="text-2xl text-gray-500 mb-8 text-left">Profit and Loss</h1>
                </div>
                <div className="flex items-center justify-center h-80">
                    <div className="text-red-500">{error}</div>
                </div>
            </div>
        );
    }

    if (!pnLData) {
        return (
            <div className="bg-white rounded-3xl p-10 max-w-6xl mx-auto shadow-lg mb-20">
                <div className="mb-8">
                    <h1 className="text-2xl text-gray-500 mb-8 text-left">Profit and Loss</h1>
                </div>
                <div className="flex items-center justify-center h-80">
                    <div className="text-gray-500">No P&L data available</div>
                </div>
            </div>
        );
    }

    const yAxisDomain = calculateYAxisDomain(pnLData.monthlyData);

    return (
        <div className="bg-white rounded-3xl p-10 max-w-6xl mx-auto shadow-lg mb-20">
            <div className="mb-8">
                <h1 className="text-2xl text-gray-500 mb-8 text-left">Profit and Loss</h1>
            </div>

            <div className="flex flex-col lg:flex-row gap-8">
                {/* Chart Section */}
                <div className="flex-1">
                    <div className="h-80 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={pnLData.monthlyData} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                                <XAxis
                                    dataKey="month"
                                    axisLine={false}
                                    tickLine={false}
                                    tick={{ fill: '#6b7280', fontSize: 12 }}
                                />
                                <YAxis
                                    axisLine={false}
                                    tickLine={false}
                                    tick={{ fill: '#6b7280', fontSize: 12 }}
                                    domain={yAxisDomain}
                                    tickFormatter={(value) => `$${value.toLocaleString()}`}
                                />
                                <Tooltip content={<CustomTooltip />} />
                                <Line
                                    type="monotone"
                                    dataKey="realized"
                                    stroke="#8b5cf6"
                                    strokeWidth={2}
                                    dot={{ fill: '#8b5cf6', strokeWidth: 2, r: 4 }}
                                    activeDot={{ r: 6, stroke: '#8b5cf6', strokeWidth: 2 }}
                                    name="Realized Gains"
                                />
                                <Line
                                    type="monotone"
                                    dataKey="unrealized"
                                    stroke="#3b82f6"
                                    strokeWidth={2}
                                    dot={{ fill: '#3b82f6', strokeWidth: 2, r: 4 }}
                                    activeDot={{ r: 6, stroke: '#3b82f6', strokeWidth: 2 }}
                                    name="Unrealized Gains"
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>

                    {/* Legend */}
                    <div className="flex items-center justify-center gap-6 mt-4">
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-purple-500"></div>
                            <span className="text-sm text-gray-600">Realized Gains</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-blue-500"></div>
                            <span className="text-sm text-gray-600">Unrealized Gains</span>
                        </div>
                    </div>
                </div>

                {/* Summary Table */}
                <div className="ml-2">
                    <div className="bg-white rounded-lg overflow-hidden shadow-sm mr-14">
                        <table className="min-w-full">
                            <thead>
                                <tr className="border-b border-gray-200">
                                    <th className="px-6 py-4 text-left text-sm font-medium text-gray-500">Gains (YTD)</th>
                                    <th className="px-6 py-4 text-right text-sm font-medium text-gray-500">Market Value $</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr className="border-b border-gray-100 font-semibold">
                                    <td className="px-6 py-4 text-sm text-gray-900 text-left">Total P&L</td>
                                    <td className="px-6 py-4 text-sm text-gray-900 text-right">
                                        ${pnLData.totalPnL.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                                    </td>
                                </tr>
                                <tr className="border-b border-gray-50">
                                    <td className="px-6 py-4 text-sm font-medium text-left text-purple-500">
                                        Realized
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-700 text-right">
                                        ${pnLData.totalRealized.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                                    </td>
                                </tr>
                                <tr className="border-b border-gray-50">
                                    <td className="px-6 py-4 text-sm font-medium text-left text-blue-500">
                                        Unrealized
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-700 text-right">
                                        ${pnLData.totalUnrealized.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}

export {Performance};