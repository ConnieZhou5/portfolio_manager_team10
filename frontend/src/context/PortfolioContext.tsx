import React, { createContext, useContext, useState, ReactNode } from 'react';

interface PortfolioContextType {
  refreshTrigger: number;
  triggerRefresh: () => void;
}

const PortfolioContext = createContext<PortfolioContextType | undefined>(undefined);

export const usePortfolio = () => {
  const context = useContext(PortfolioContext);
  if (context === undefined) {
    throw new Error('usePortfolio must be used within a PortfolioProvider');
  }
  return context;
};

interface PortfolioProviderProps {
  children: ReactNode;
}

export const PortfolioProvider: React.FC<PortfolioProviderProps> = ({ children }) => {
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const triggerRefresh = () => {
    setRefreshTrigger(prev => prev + 1);
  };

  return (
    <PortfolioContext.Provider value={{ refreshTrigger, triggerRefresh }}>
      {children}
    </PortfolioContext.Provider>
  );
}; 