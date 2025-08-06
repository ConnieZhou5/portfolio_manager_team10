import React, { useState } from 'react';
import { TrendingUp, TrendingDown, BarChart3, Newspaper, Brain, Zap } from 'lucide-react';


const AIInsightsCard = ({ aiAnalysis }) => {
  const [showReasoning, setShowReasoning] = useState(false);

  const analysisData = {
    recommendation: aiAnalysis.recommendation || 'Buy',
    techData: aiAnalysis.techData || 'Positive',
    newsData: aiAnalysis.newsData || 'Positive',
    aiAnalysis: aiAnalysis.aiAnalysis || 'Positive',
    reasoning: aiAnalysis.reasoning || [
      'Strong technical indicators',
      'Good news',
      'Buy',
    ]
  };

  const toggleReasoning = () => {
    setShowReasoning(prev => !prev);
  };

  const getColor = (sentiment) => {
    switch (sentiment?.toLowerCase()) {
      case 'positive', 'buy':
        return 'bg-gradient-to-r from-green-100 to-green-50 text-green-700 border-green-200';
      case 'negative', 'sell':
        return 'bg-gradient-to-r from-red-100 to-red-50 text-red-700 border-red-200';
      default:
        return 'bg-gradient-to-r from-yellow-100 to-yellow-50 text-yellow-700 border-yellow-200';
    }
  };

  const getRecommendationIcon = (recommendation) => {
    switch (recommendation?.toLowerCase()) {
      case 'buy':
        return <TrendingUp className="w-5 h-5" />;
      case 'sell':
        return <TrendingDown className="w-5 h-5" />;
      default:
        return;
    }
  };

  const InsightRow = ({ label, value, icon }) => (
    <div className="group flex justify-between items-center px-4 py-2 rounded-3xl bg-gradient-to-br from-white via-purple-50 to-purple-100 shadow-sm border border-purple-100">
      <div className="flex items-center gap-2">
        <div className="text-gray-500">{icon}</div>
        <span className="font-medium text-gray-500">{label}</span>
      </div>
      <span className={`text-xs px-4 py-2 rounded-full font-semibold border transition-all duration-300 ${getColor(value)}`}>
        {value}
      </span>
    </div>);

  return (
    <div className="mt-8 mb-8 bg-white rounded-3xl p-8 w-[600px] shadow-sm mx-auto text-gray-800 space-y-6 border border-purple-100">
      <div className="flex items-center gap-2 bg-gradient-to-br from-purple-500 via-purple-500 to-purple-600 rounded-2xl p-4 text-left">
        <Zap className="w-5 h-5 text-white" />
        <h3 className="text-white text-xl font-bold">AI Insights</h3>
      </div>


      <div className="space-y-4">
        <p className="text-lg font-semibold text-gray-600">Recommendation</p>
        <div className={`inline-flex items-center gap-2 px-8 py-4 rounded-3xl font-bold text-2xl shadow-md ${getColor(analysisData.recommendation)}`}>
          {getRecommendationIcon(analysisData.recommendation)}
          <span>{analysisData.recommendation}</span>
        </div>
      </div>

      <div className="bg-white space-y-2">
        <InsightRow label="Technical Analysis" value={analysisData.techData} icon={<BarChart3 className="w-4 h-4" />} />
        <InsightRow label="News Analysis" value={analysisData.newsData} icon={<Newspaper className="w-4 h-4" />} />
        <InsightRow label="AI Analysis" value={analysisData.aiAnalysis} icon={<Zap className="w-4 h-4" />} />
        <button onClick={toggleReasoning} className="text-purple-500 font-bold text-sm hover:text-purple-700">
          {showReasoning ? 'Hide Reasoning' : 'Show Reasoning'}
        </button>
      </div>

      {showReasoning && (
        <div className="bg-gradient-to-br from-white via-purple-50 to-purple-100 shadow-sm border border-purple-100 rounded-3xl p-6 text-left">
          <h4 className="font-bold text-gray-500 mb-4 flex items-center gap-2">
            <Brain className="w-4 h-4" />
            Detailed Reasoning
          </h4>
          {Array.isArray(analysisData.reasoning) ? (
            <ul className="list-disc list-inside space-y-1 mt-1 text-gray-500">
              {analysisData.reasoning.map((point, idx) => (
                <li key={idx}>{point.replace(/^[•\-\s]+/, '').trim()}</li>
              ))}
            </ul>
          ) : (
              <ul className="list-disc list-inside space-y-1 mt-1 text-gray-500">
                {analysisData.reasoning
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