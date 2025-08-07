import React, { useState } from 'react';

// Utility: aggregate data
const groupAndAverageBySymbol = (data) => {
  if (!Array.isArray(data)) return [];

  const grouped = {};

  data.forEach((row) => {
    const { symbol, ...rest } = row;

    if (!grouped[symbol]) {
      grouped[symbol] = { count: 0, ...Object.fromEntries(Object.keys(rest).map(k => [k, 0])) };
    }

    grouped[symbol].count += 1;
    for (const key in rest) {
      grouped[symbol][key] += rest[key];
    }
  });

  return Object.entries(grouped).map(([symbol, agg]) => {
    const result = { symbol };

    for (const key in agg) {
      if (key === 'count') continue;
      if (key === 'quantity') {
        result[key] = agg[key]; // sum quantity
      } else {
        result[key] = +(agg[key] / agg.count).toFixed(2); // average other fields
      }
    }

    return result;
  });
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2
  }).format(value);
};

const formatPercentage = (value) => {
  return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
};

const getValueColor = (value) => {
  if (value > 0) return 'text-green-600';
  if (value < 0) return 'text-red-600';
  return 'text-gray-700';
};

const PivotTable = ({ data, searchText }) => {
  const [expandedSymbols, setExpandedSymbols] = useState(new Set());

  const toggleSymbol = (symbol) => {
    setExpandedSymbols(prev => {
      const newSet = new Set(prev);
      newSet.has(symbol) ? newSet.delete(symbol) : newSet.add(symbol);
      return newSet;
    });
  };

  const groupedData = groupAndAverageBySymbol(data);

  const rawBySymbol = data.reduce((acc, row) => {
    if (!acc[row.symbol]) acc[row.symbol] = [];
    acc[row.symbol].push(row);
    return acc;
  }, {});

  const filteredGroupedData = groupedData.filter((row) =>
    row.symbol.toLowerCase().includes(searchText.toLowerCase())
  );

  // ✅ Global totals for last 5 columns
  const filteredRawRows = data.filter((row) =>
    row.symbol.toLowerCase().includes(searchText.toLowerCase())
  );

  const globalTotals = filteredRawRows.reduce(
    (acc, row) => {
      // Calculate total amount invested (price paid × quantity) for each stock
      acc.pricePaid += row.pricePaid * row.quantity;
      acc.daysGain += row.daysGain;
      acc.totalGain += row.totalGain;
      acc.value += row.value;
      return acc;
    },
    { pricePaid: 0, daysGain: 0, totalGain: 0, totalGainPercent: 0, value: 0 }
  );

  // Calculate weighted average percentage based on total gain vs total invested
  if (globalTotals.pricePaid > 0) {
    globalTotals.totalGainPercent = (globalTotals.totalGain / globalTotals.pricePaid) * 100;
  }

  for (const key in globalTotals) {
    globalTotals[key] = +globalTotals[key].toFixed(2);
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-gray-700 min-w-[120px]">
                Symbol
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[100px]">
                Latest Price$
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[90px]">
                Change$
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[90px]">
                Change%
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[80px]">
                Qty
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[100px]">
                Price Paid$
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[100px]">
                Day's Gain$
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[100px]">
                Total Gain$
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[100px]">
                Total Gain%
              </th>
              <th className="px-4 py-3 text-right font-semibold text-gray-700 min-w-[120px]">
                Value$
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {filteredGroupedData.map((row, idx) => {
              const isExpanded = expandedSymbols.has(row.symbol);

              return (
                <React.Fragment key={idx}>
                  <tr
                    className="border-b cursor-pointer hover:bg-gray-200"
                    onClick={() => toggleSymbol(row.symbol)}
                  >
                    <td className="px-4 py-3">
                      <div className="flex items-center space-x-2">

                        <span className="font-semibold text-gray-900">{row.symbol}</span>

                      </div>
                    </td>
                    <td className="px-4 py-3 text-right font-medium text-gray-900">
                      {formatCurrency(row.lastPrice || 0)}
                    </td>
                    <td className={`px-4 py-3 text-right font-medium ${getValueColor(row.change || 0)}`}>

                      <span>{formatCurrency(row.change || 0)}</span>

                    </td>
                    <td className={`px-4 py-3 text-right font-medium ${getValueColor(row.changePercent || 0)}`}>
                      {formatPercentage(row.changePercent || 0)}
                    </td>
                    <td className="px-4 py-3 text-right text-gray-700">
                      {(row.quantity || 0).toLocaleString()}
                    </td>
                    <td className="px-4 py-3 text-right text-gray-700">
                      {formatCurrency(row.pricePaid || 0)}
                    </td>
                    <td className={`px-4 py-3 text-right font-medium ${getValueColor(row.daysGain || 0)}`}>
                      {formatCurrency(row.daysGain || 0)}
                    </td>
                    <td className={`px-4 py-3 text-right font-medium ${getValueColor(row.totalGain || 0)}`}>
                      {formatCurrency(row.totalGain || 0)}
                    </td>
                    <td className={`px-4 py-3 text-right font-medium ${getValueColor(row.totalGainPercent || 0)}`}>
                      {formatPercentage(row.totalGainPercent || 0)}
                    </td>
                    <td className="px-4 py-3 text-right font-semibold text-gray-900">
                      {formatCurrency(row.value || 0)}
                    </td>
                  </tr>

                  {isExpanded &&
                    rawBySymbol[row.symbol]?.map((rawRow, subIdx) => (
                      <tr key={`${idx}-${subIdx}`} className="border-b bg-gray-100 text-gray-600">
                        <td className="pl-4 pr-4 py-2">
                          <div className="text-xs text-gray-500 text-left">
                            {rawRow.date || `Position ${subIdx + 1}`}
                          </div>
                        </td>
                        <td className="px-4 py-2 text-right text-sm text-gray-700">
                          {formatCurrency(rawRow.lastPrice || 0)}
                        </td>
                        <td className={`px-4 py-2 text-right text-sm ${getValueColor(rawRow.change || 0)}`}>
                          {formatCurrency(rawRow.change || 0)}
                        </td>
                        <td className={`px-4 py-2 text-right text-sm ${getValueColor(rawRow.changePercent || 0)}`}>
                          {formatPercentage(rawRow.changePercent || 0)}
                        </td>
                        <td className="px-4 py-2 text-right text-sm text-gray-700">
                          {(rawRow.quantity || 0).toLocaleString()}
                        </td>
                        <td className="px-4 py-2 text-right text-sm text-gray-700">
                          {formatCurrency(rawRow.pricePaid || 0)}
                        </td>
                        <td className={`px-4 py-2 text-right text-sm ${getValueColor(rawRow.daysGain || 0)}`}>
                          {formatCurrency(rawRow.daysGain || 0)}
                        </td>
                        <td className={`px-4 py-2 text-right text-sm ${getValueColor(rawRow.totalGain || 0)}`}>
                          {formatCurrency(rawRow.totalGain || 0)}
                        </td>
                        <td className={`px-4 py-2 text-right text-sm ${getValueColor(rawRow.totalGainPercent || 0)}`}>
                          {formatPercentage(rawRow.totalGainPercent || 0)}
                        </td>
                        <td className="px-4 py-2 text-right text-sm font-medium text-gray-700">
                          {formatCurrency(rawRow.value || 0)}
                        </td>
                      </tr>
                    ))}
                </React.Fragment>
              );
            })}


            {/* Global totals row */}
            <tr className="bg-gray-200 text-black font-semibold border-t-2 border-gray-300">
              <td className="px-4 py-4" colSpan={5}>
                <div className="flex items-center space-x-2">
                  <span>Total</span>
                </div>
              </td>
              <td className="px-4 py-4 text-right">{formatCurrency(globalTotals.pricePaid)}</td>
              <td className={`px-4 py-4 text-right ${getValueColor(globalTotals.daysGain)}`}>
                {formatCurrency(globalTotals.daysGain)}
              </td>
              <td className={`px-4 py-4 text-right ${getValueColor(globalTotals.totalGain)}`}>
                {formatCurrency(globalTotals.totalGain)}
              </td>
              <td className={`px-4 py-4 text-right ${getValueColor(globalTotals.totalGainPercent)}`}>
                {formatPercentage(globalTotals.totalGainPercent)}
              </td>
              <td className="px-4 py-4 text-right font-bold text-md">{formatCurrency(globalTotals.value)}</td>
            </tr>
          </tbody>
        </table>
      </div>

      {filteredGroupedData.length === 0 && searchText && (
        <div className="text-center py-8 text-gray-500">
          <p>No results found for "{searchText}"</p>
        </div>
      )}
    </div>
  );
};

export default PivotTable;