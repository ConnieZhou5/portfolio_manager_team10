import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from 'recharts';

const performanceData = [
    { month: 'Jan', realized: 0, unrealized: 100 },
    { month: 'Feb', realized: 120, unrealized: 340 },
    { month: 'Mar', realized: 200, unrealized: 520 },
    { month: 'Apr', realized: 200, unrealized: 660 },
    { month: 'May', realized: 320, unrealized: 820 },
    { month: 'Jun', realized: 330, unrealized: 860 },
    { month: 'Jul', realized: 330, unrealized: 950 }
];

export default function Performance() {
    return (

        <div className="bg-white rounded-2xl p-10 max-w-6xl mx-auto shadow-lg mb-20">
            <div className="mb-8">
                <h1 className="text-2xl text-gray-500 mb-8 text-left">Profit and Loss</h1>
            </div>

            <div className="flex flex-col lg:flex-row gap-8">
                {/* Chart Section */}
                <div className="flex-1">
                    <div className="h-80 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={performanceData} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
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
                                    domain={[0, 1000]}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="realized"
                                    stroke="#8b5cf6"
                                    strokeWidth={2}
                                    dot={false}
                                    name="Realized Gains"
                                />
                                <Line
                                    type="monotone"
                                    dataKey="unrealized"
                                    stroke="#3b82f6"
                                    strokeWidth={2}
                                    dot={false}
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
                                        $1,210.00
                                    </td>
                                </tr>
                                <tr className="border-b border-gray-50">
                                    <td className="px-6 py-4 text-sm font-medium text-left text-purple-500">
                                        Realized
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-700 text-right">
                                        $260.00
                                    </td>
                                </tr>
                                <tr className="border-b border-gray-50">
                                    <td className="px-6 py-4 text-sm font-medium text-left text-blue-500">
                                        Unrealized
                                    </td>
                                    <td className="px-6 py-4 text-sm text-gray-700 text-right">
                                        $950.00
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