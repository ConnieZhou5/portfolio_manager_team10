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
      acc.pricePaid += row.pricePaid;
      acc.daysGain += row.daysGain;
      acc.totalGain += row.totalGain;
      acc.totalGainPercent += row.totalGainPercent;
      acc.value += row.value;
      return acc;
    },
    { pricePaid: 0, daysGain: 0, totalGain: 0, totalGainPercent: 0, value: 0 }
  );

  for (const key in globalTotals) {
    globalTotals[key] = +globalTotals[key].toFixed(2);
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-xs text-left">
        <thead className="text-gray-700 border-b align-center h-8">
          <tr>
            <th className="px-2 py-2">Symbol</th>
            <th className="px-2 py-2">Last Price $</th>
            <th className="px-2 py-2">Change $</th>
            <th className="px-2 py-2">Change %</th>
            <th className="px-2 py-2">Qty #</th>
            <th className="px-2 py-2">Price Paid $</th>
            <th className="px-2 py-2">Day's Gain $</th>
            <th className="px-2 py-2">Total Gain $</th>
            <th className="px-2 py-2">Total Gain %</th>
            <th className="px-2 py-2">Value $</th>
          </tr>
        </thead>
        <tbody>
          {filteredGroupedData.map((row, idx) => {
            const isExpanded = expandedSymbols.has(row.symbol);
            return (
              <React.Fragment key={idx}>
                <tr
                  className="border-b cursor-pointer hover:bg-gray-200"
                  onClick={() => toggleSymbol(row.symbol)}
                >
                  <td className="px-2 py-2 font-semibold">{row.symbol}</td>
                  <td className="px-2 py-2">{row.lastPrice}</td>
                  <td className="px-2 py-2">{row.change}</td>
                  <td className="px-2 py-2">{row.changePercent}</td>
                  <td className="px-2 py-2">{row.quantity}</td>
                  <td className="px-2 py-2">{row.pricePaid}</td>
                  <td className="px-2 py-2">{row.daysGain}</td>
                  <td className="px-2 py-2">{row.totalGain}</td>
                  <td className="px-2 py-2">{row.totalGainPercent}</td>
                  <td className="px-2 py-2">{row.value}</td>
                </tr>

                {isExpanded &&
                  rawBySymbol[row.symbol]?.map((rawRow, subIdx) => (
                    <tr key={`${idx}-${subIdx}`} className="border-b bg-white text-gray-600">
                      <td className="px-2 py-2 text-xs text-gray-500">{rawRow.date}</td>
                      <td className="px-2 py-2">{rawRow.lastPrice}</td>
                      <td className="px-2 py-2">{rawRow.change}</td>
                      <td className="px-2 py-2">{rawRow.changePercent}</td>
                      <td className="px-2 py-2">{rawRow.quantity}</td>
                      <td className="px-2 py-2">{rawRow.pricePaid}</td>
                      <td className="px-2 py-2">{rawRow.daysGain}</td>
                      <td className="px-2 py-2">{rawRow.totalGain}</td>
                      <td className="px-2 py-2">{rawRow.totalGainPercent}</td>
                      <td className="px-2 py-2">{rawRow.value}</td>
                      {/* Date column (raw row only) */}
                      
                    </tr>
                  ))}

              </React.Fragment>
            );
          })}

          {/* ✅ Global totals row */}
          <tr className="px-2 py-2 font-semibold bg-black-100 text-black-800 border-t">
            <td className="px-2 py-2" colSpan={5}>Totals</td>
            <td className="px-2 py-2">{globalTotals.pricePaid}</td>
            <td className="px-2 py-2">{globalTotals.daysGain}</td>
            <td className="px-2 py-2">{globalTotals.totalGain}</td>
            <td className="px-2 py-2">{globalTotals.totalGainPercent}</td>
            <td className="px-2 py-2">{globalTotals.value}</td>
          </tr>
        </tbody>
      </table>
    </div>
  );
};

export default PivotTable;
