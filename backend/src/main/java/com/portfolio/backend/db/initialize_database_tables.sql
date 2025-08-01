CREATE DATABASE IF NOT EXISTS portfolio_db;
USE portfolio_db;

CREATE TABLE portfolio_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ticker VARCHAR(10) NOT NULL,
  quantity INT NOT NULL,
  buyPrice DECIMAL(10,2) NOT NULL,
  buy_date DATE NOT NULL
);

-- Daily values (keep 30 days)
CREATE TABLE portfolio_daily_values (
    id BIGINT PRIMARY KEY,
    snapshot_date DATE NOT NULL,
    total_value DECIMAL(10,2) NOT NULL,
    investments_value DECIMAL(10,2) NOT NULL,
    cash_value DECIMAL(10,2) NOT NULL
);

-- Monthly summaries (keep 1 year)
CREATE TABLE portfolio_monthly_summaries (
    id BIGINT PRIMARY KEY,
    year INT NOT NULL,
    month INT NOT NULL,
    total_value DECIMAL(10,2) NOT NULL,
    monthly_gain DECIMAL(10,2) NOT NULL,
    monthly_gain_percentage DECIMAL(10,2) NOT NULL
);

CREATE TABLE cash_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    balance DECIMAL(10,2) NOT NULL,
    last_updated DATETIME NOT NULL
);

CREATE TABLE trade_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    trade_date DATE NOT NULL,
    ticker VARCHAR(12) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    trade_type ENUM('BUY', 'SELL') NOT NULL
);
