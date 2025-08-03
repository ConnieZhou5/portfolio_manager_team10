import React, { useState, useEffect } from 'react';
import { apiService, PortfolioStats, PortfolioItem } from '../services/api';
import { usePortfolio } from '../context/PortfolioContext';

type ChangeType = 'positive' | 'negative' | 'neutral'

interface Properties {
    title: string;
    value: string;
    change?: string;
    changeType?: ChangeType;
    icon?: React.ReactNode;
    onClick?: () => void;
    clickable?: boolean;
}

interface CashModalProps {
    isOpen: boolean;
    onClose: () => void;
    onAddCash: (amount: number) => void;
    currentCash: string;
}

const CashModal: React.FC<CashModalProps> = ({ isOpen, onClose, onAddCash, currentCash }) => {
    const [amount, setAmount] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!amount || isNaN(Number(amount))) return;

        setIsLoading(true);
        try {
            await onAddCash(Number(amount));
            setAmount('');
            onClose();
        } catch (error) {
            console.error('Error adding cash:', error);
        } finally {
            setIsLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-96 max-w-md mx-4">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">Add Cash</h3>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>
                
                <div className="mb-4">
                    <p className="text-sm text-gray-600 mb-2">Current Cash: {currentCash}</p>
                    <form onSubmit={handleSubmit}>
                        <div className="mb-4">
                            <label htmlFor="amount" className="block text-sm font-medium text-gray-700 mb-2">
                                Amount to Add ($)
                            </label>
                            <input
                                type="number"
                                id="amount"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                                placeholder="Enter amount"
                                step="0.01"
                                min="0"
                                required
                            />
                        </div>
                        
                        <div className="flex justify-end space-x-3">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-500"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={isLoading || !amount}
                                className="px-4 py-2 text-sm font-medium text-white bg-purple-600 rounded-md hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {isLoading ? 'Adding...' : 'Add Cash'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

const StatsCard: React.FC<Properties> = ({ title, value, change, changeType, icon, onClick, clickable }) => {
    //change color for the icon based on if day's gain is profit or loss 
    const getChangeColor = () => {
        if (changeType === 'positive') return 'text-green-600';
        if (changeType === 'negative') return 'text-red-600';
        return 'text-gray-600';
    };

    //change border color based on if day's gain is profit or loss
    const getBorderColor = () => {
        if (title === 'Total Assets') return 'border-l-purple-500';
        if (title === 'Investments') return 'border-l-purple-500';
        if (title === 'Day\'s Gain') {
            return changeType === 'positive' ? 'border-l-green-500' : 'border-l-red-500';
        }
        if (title === 'Cash') return 'border-l-purple-500';
        return 'border-l-purple-500';
    };

    const handleClick = () => {
        if (clickable && onClick) {
            onClick();
        }
    };

    return (
        <div 
            className={`bg-white rounded-lg p-4 shadow-sm border-l-4 ${getBorderColor()} hover:shadow-md transition-shadow duration-200 ${clickable ? 'cursor-pointer hover:bg-gray-50' : ''}`}
            onClick={handleClick}
        >
            <div className="flex items-center justify-between">
                <div className="flex-1">
                    <p className="text-2xl text-gray-600 mb-2 text-left">{title}</p>
                    <div className="flex items-baseline gap-2">
                        <p className="text-2xl font-bold text-gray-900">{value}</p>
                        {change && (
                            <p className={`text-sm font-medium ${getChangeColor()}`}>
                                {change}
                            </p>
                        )}
                    </div>
                </div>
                {icon && (
                    <div className="ml-4 flex-shrink-0">
                        <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
                            {icon}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

const PortfolioStatsCards = () => {
    const [stats, setStats] = useState<PortfolioStats | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isCashModalOpen, setIsCashModalOpen] = useState(false);
    const { refreshTrigger } = usePortfolio();

    const handleAddCash = async (amount: number) => {
        try {
            await apiService.addCash(amount);
            // Refresh the stats to show updated cash balance
            const updatedStats = await apiService.getPortfolioStats();
            setStats(updatedStats);
        } catch (error) {
            console.error('Error adding cash:', error);
            throw error;
        }
    };

    const handleCashCardClick = () => {
        setIsCashModalOpen(true);
    };

    useEffect(() => {
        const fetchStats = async () => {
            try {
                setLoading(true);
                setError(null);
                
                // Fetch portfolio stats and portfolio items
                const [portfolioStats, portfolioItems] = await Promise.all([
                    apiService.getPortfolioStats(),
                    apiService.getAllPortfolioItems()
                ]);

                // Calculate investments value using current market prices
                let investmentsValue = 0;
                if (portfolioItems.length > 0) {
                    try {
                        const symbols = portfolioItems.map(item => item.ticker);
                        const stockData = await apiService.getStockData(symbols);
                        
                        // Calculate investments value using current market prices
                        investmentsValue = portfolioItems.reduce((total, item) => {
                            const currentStock = stockData.find(stock => stock.symbol === item.ticker);
                            const currentPrice = currentStock?.price || item.buyPrice;
                            return total + (currentPrice * item.quantity);
                        }, 0);
                    } catch (err) {
                        console.error('Error fetching stock data for stats, using buy prices as fallback:', err);
                        // Fallback to buy prices if stock data fetch fails
                        investmentsValue = portfolioItems.reduce((total, item) => {
                            return total + (item.buyPrice * item.quantity);
                        }, 0);
                    }
                } else {
                    // Use portfolio stats investments value if no portfolio items
                    investmentsValue = parseFloat(portfolioStats.investments.replace('$', '').replace(',', ''));
                }

                // Parse cash value
                const cashValue = parseFloat(portfolioStats.cash.replace('$', '').replace(',', ''));
                const totalAssetsValue = cashValue + investmentsValue;

                // Create updated stats object with market-based investments value
                const updatedStats: PortfolioStats = {
                    ...portfolioStats,
                    investments: `$${investmentsValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
                    totalAssets: `$${totalAssetsValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
                };

                setStats(updatedStats);
            } catch (err) {
                setError('Failed to load portfolio statistics. Check if backend is running.');
                console.error('Error fetching stats:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [refreshTrigger]); // Add refreshTrigger as dependency

    if (loading) {
        return (
            <div className="p-6">
                <div className="max-w-6xl mx-auto">
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                        {[...Array(4)].map((_, index) => (
                            <div key={index} className="bg-white rounded-lg p-4 shadow-sm border-l-4 border-l-purple-500 animate-pulse">
                                <div className="h-8 bg-gray-200 rounded mb-2"></div>
                                <div className="h-6 bg-gray-200 rounded"></div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-6">
                <div className="max-w-6xl mx-auto">
                    <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                        <p className="text-red-800">{error}</p>
                    </div>
                </div>
            </div>
        );
    }

    if (!stats) {
        return null;
    }

    return (
        <div className="p-6">
            <div className="max-w-6xl mx-auto">

                {/* Stats Cards Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    <StatsCard
                        title="Total Assets"
                        value={stats.totalAssets}
                        icon={
                            <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                            </svg>
                        }
                    />

                    <StatsCard
                        title="Investments"
                        value={stats.investments}
                        icon={
                            <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                            </svg>
                        }
                    />

                    <StatsCard
                        title="Day's Gain"
                        value={stats.daysGain}
                        change={`(${stats.daysGainPercentage.startsWith('-') ? '' : '+'}${stats.daysGainPercentage})`}
                        changeType={stats.daysGain.startsWith('-$') || stats.daysGain.startsWith('-') ? 'negative' : 'positive'}
                        icon={
                            <svg className={`w-6 h-6 ${stats.daysGain.startsWith('-$') || stats.daysGain.startsWith('-') ? 'text-red-600' : 'text-green-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" />
                            </svg>
                        }
                    />

                    <StatsCard
                        title="Cash"
                        value={stats.cash}
                        clickable={true}
                        onClick={handleCashCardClick}
                        icon={
                            <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                            </svg>
                        }
                    />
                </div>
            </div>
            
            {/* Cash Modal */}
            <CashModal
                isOpen={isCashModalOpen}
                onClose={() => setIsCashModalOpen(false)}
                onAddCash={handleAddCash}
                currentCash={stats.cash}
            />
        </div>
    );
};

export { PortfolioStatsCards };