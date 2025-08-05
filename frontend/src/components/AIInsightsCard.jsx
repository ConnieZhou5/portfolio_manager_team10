import React, { useState } from 'react';
import './AIInsightsCard.css'; // optional for styles

const AIInsightsCard = ({ aiAnalysis }) => {
  const [showRecommendation, setShowRecommendation] = useState(false);

  const toggleRecommendation = () => {
    setShowRecommendation(prev => !prev);
  };

  const sentimentColor = (sentiment) => {
    switch (sentiment.toLowerCase()) {
      case 'positive':
        return 'green';
      case 'negative':
        return 'red';
      case 'neutral':
      case 'mixed':
      default:
        return 'orange';
    }
  };

  const sentiment = aiAnalysis?.generalAnalysis?.includes('recommendation')
    ? (aiAnalysis.generalAnalysis.match(/recommendation would be to \*\*(\w+)\*\*/i)?.[1] || 'Mixed')
    : 'Mixed';

  return (
    <div className="ai-insight-card">
      <h3>AI Stock Analysis</h3>

      <button onClick={toggleRecommendation} className="toggle-button">
        {showRecommendation ? 'Hide Recommendation' : 'Show Recommendation'}
      </button>

      {showRecommendation && (
        <p style={{ color: sentimentColor(sentiment) }}>
          {aiAnalysis.generalAnalysis}
        </p>
      )}

      <div className="meta-info">
        <p><strong>Analyst Rating:</strong> {aiAnalysis.analystRating}</p>
        <p><strong>News:</strong> {aiAnalysis.newsSentiment}</p>
        <p><strong>Social Media:</strong> {aiAnalysis.socialSentiment}</p>
        <p><strong>Technical Analysis:</strong> {aiAnalysis.technicalSignal}</p>
      </div>
    </div>
  );
};

export default AIInsightsCard;
