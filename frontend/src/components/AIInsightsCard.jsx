import React, { useState } from 'react';

const AIInsightsCard = ({ aiAnalysis }) => {
  const [showReasoning, setShowReasoning] = useState(false);

  const toggleReasoning = () => {
    setShowReasoning(prev => !prev);
  };

  const getColor = (sentiment) => {
    switch (sentiment?.toLowerCase()) {
      case 'positive':
        return 'bg-green-100 text-green-600';
      case 'negative':
        return 'bg-red-100 text-red-600';
      case 'neutral':
      default:
        return 'bg-yellow-100 text-yellow-600';
    }
  };

  const InsightRow = ({ label, value }) => (
    <div className="flex justify-between items-center px-4 py-2 rounded-full bg-gradient-to-br from-white to-purple-200 shadow-sm">
      <span>{label}</span>
      <span className={`text-xs px-3 py-1 rounded-full ${getColor(value)}`}>
        {value}
      </span>
    </div>
  );

  return (
    <div className="bg-white rounded-3xl shadow-xl p-6 w-full max-w-md mx-auto text-sm text-gray-800 font-medium space-y-4">
      <div className="bg-purple-600 flex flex-col items-center rounded-xl py-2">
        <h3 className="text-white text-lg text-center">AI Insights</h3>
      </div>

      <div className="text-center">
        <p className="text-sm text-gray-700">Recommendation</p>
        <div className="inline-block px-6 py-1 mt-1 rounded-full bg-green-100 text-green-600 font-bold text-xl">
          {aiAnalysis?.recommendation}
        </div>
      </div>

      <div className="space-y-2">
        <InsightRow label="Technical Analysis" value={aiAnalysis?.techData} />
        <InsightRow label="News Analysis" value={aiAnalysis?.newsData} />
        <InsightRow label="AI Analysis" value={aiAnalysis?.aiAnalysis} />
        <button onClick={toggleReasoning} className="text-gray-700 text-sm">
          {showReasoning ? 'Hide Reasoning' : 'Show Reasoning'}
        </button>
      </div>

      {showReasoning && (
        <div className="bg-white/30 rounded-xl text-gray-700 text-xs p-4 text-justify">
          <p className="font-semibold">Reasoning:</p>
          {Array.isArray(aiAnalysis?.reasoning) ? (
            <ul className="list-disc list-inside space-y-1 mt-1">
              {aiAnalysis.reasoning.map((point, idx) => (
                <li key={idx}>{point.replace(/^[•\-\s]+/, '').trim()}</li>
              ))}
            </ul>
          ) : (
            <ul className="list-disc list-inside space-y-1 mt-1">
              {aiAnalysis?.reasoning
                ?.split(/\n+|\.\s+/) // split by newlines or sentence ends
                .filter((line) => line.trim().length > 0)
                .map((point, idx) => (
                  <li key={idx}>{point.replace(/^[•\-\s]+/, '').trim()}</li>
                ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
};

export default AIInsightsCard;