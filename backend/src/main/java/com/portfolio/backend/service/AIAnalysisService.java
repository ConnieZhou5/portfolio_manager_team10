package com.portfolio.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.config.FmpConfig;
import com.portfolio.backend.config.NewsConfig;
import com.portfolio.backend.config.CohereConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIAnalysisService {
        @Value("${fmp.api-key}")
        private String fmpApiKey;

        @Value("${news.api-key}")
        private String newsApiKey;

        @Value("${cohere.api-key}")
        private String cohereApiKey;

        @Autowired
        private FmpConfig fmpConfig;

        @Autowired
        private NewsConfig newsConfig;

        @Autowired
        private CohereConfig cohereConfig;

        private final RestTemplate restTemplate = new RestTemplate();

        public Map<String, Object> getAnalysis(String symbol) {
                try {
                        String techUrl = String.format("https://financialmodelingprep.com/api/v3/quote/%s?apikey=%s",
                                        symbol,
                                        fmpApiKey);
                        String newsUrl = String.format("https://newsapi.org/v2/everything?q=%s&apiKey=%s", symbol,
                                        newsApiKey);

                        JsonNode techData = restTemplate.getForObject(techUrl, JsonNode.class);
                        JsonNode newsData = restTemplate.getForObject(newsUrl, JsonNode.class);

                        String aiPrompt = String.format(
                                        """
                                                        Based on the following stock data and news, provide an investment recommendation for the stock symbol %s.

                                                        Please return the result strictly in the following JSON format:

                                                        {
                                                          "techData": "Positive | Neutral | Negative",
                                                          "newsData": "Positive | Neutral | Negative",
                                                          "aiAnalysis": "Positive | Neutral | Negative",
                                                          "recommendation": "BUY | HOLD | SELL",
                                                          "reasoning": "<A short explanation combining technicals and news to justify the recommendation. Return concise bullet points, each starting with a bullet point (•)>"
                                                        }

                                                        Stock Data:
                                                        %s

                                                        News Articles:
                                                        %s
                                                        """,
                                        symbol, techData.toPrettyString(), newsData.get("articles").toPrettyString());

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.setBearerAuth(cohereApiKey);

                        Map<String, Object> payload = new HashMap<>();
                        payload.put("message", aiPrompt);
                        payload.put("model", "command-r"); // free-tier model
                        payload.put("chat_history", new ArrayList<>()); // optional, empty

                        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                        ResponseEntity<JsonNode> response = restTemplate.postForEntity("https://api.cohere.ai/v1/chat",
                                        request,
                                        JsonNode.class);

                        String aiAnalysisText = response.getBody().has("text")
                                        ? response.getBody().get("text").asText()
                                        : response.getBody().get("generations").get(0).get("text").asText();

                        ObjectMapper mapper = new ObjectMapper();
                        // Remove Markdown formatting if it exists
                        String cleanedJson = aiAnalysisText
                                        .replaceAll("(?s)```json", "")
                                        .replaceAll("(?s)```", "")
                                        .trim();

                        JsonNode parsed = mapper.readTree(cleanedJson);

                        Map<String, Object> result = new HashMap<>();
                        result.put("techData", parsed.get("techData").asText());
                        result.put("newsData", parsed.get("newsData").asText());
                        result.put("aiAnalysis", parsed.get("aiAnalysis").asText());
                        result.put("recommendation", parsed.get("recommendation").asText());
                        result.put("reasoning", parsed.get("reasoning").asText());

                        return result;
                } catch (Exception e) {
                        e.printStackTrace();
                        return Map.of("error", "Failed to generate AI analysis.");
                }
        }
}
