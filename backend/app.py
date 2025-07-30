from flask import Flask, jsonify, request
import yfinance as yf
from flask_cors import CORS

app = Flask(__name__)
CORS(app)  # Allow cross-origin requests

@app.route('/api/portfolio', methods=['POST'])
def get_portfolio_data():
    data = request.get_json()
    symbols = data.get('symbols', [])

    portfolio_data = []
    for symbol in symbols:
        try:
            stock = yf.Ticker(symbol)
            info = stock.info
            
            # If 'regularMarketPrice' or 'shortName' is missing, it's likely an invalid symbol
            if not info or 'regularMarketPrice' not in info:
                raise ValueError(f"Symbol '{symbol}' not found or has no market data.")
            
            portfolio_data.append({
                'symbol': symbol,
                'name': info.get('shortName'),
                'price': info.get('regularMarketPrice'),
                'currency': info.get('currency'),
                'marketCap': info.get('marketCap')
            })
        except Exception as e:
            portfolio_data.append({
                'symbol': symbol,
                'name': None,
                'price': None,
                'currency': None,
                'marketCap': None,
                'error': str(e)
            })

    return jsonify(portfolio_data)

if __name__ == '__main__':
    app.run(debug=True)
