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
            // Fetch stock and news data
            String techUrl = String.format("https://financialmodelingprep.com/api/v3/quote/%s?apikey=%s", symbol, fmpApiKey);
            String newsUrl = String.format("https://newsapi.org/v2/everything?q=%s&apiKey=%s", symbol, newsApiKey);

            JsonNode techData = restTemplate.getForObject(techUrl, JsonNode.class);
            JsonNode newsData = restTemplate.getForObject(newsUrl, JsonNode.class);

            // Updated prompt
            String aiPrompt = String.format(
                """
                Based on the following stock data and news, provide an investment recommendation for the stock symbol %s.

                You must return the response strictly in the following JSON format. Do not include markdown, triple backticks, or any other formatting:

                {
                  "techData": "Positive | Neutral | Negative",
                  "newsData": "Positive | Neutral | Negative",
                  "aiAnalysis": "Positive | Neutral | Negative",
                  "recommendation": "BUY | HOLD | SELL",
                  "reasoning": "<Concise bullet points.>"
                }

                Stock Data:
                %s

                News Articles:
                %s
                """,
                symbol, techData.toPrettyString(), newsData.get("articles").toPrettyString());

            // Request setup
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cohereApiKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", aiPrompt);
            payload.put("model", "command-r");
            payload.put("chat_history", new ArrayList<>());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Call Cohere API
            ResponseEntity<JsonNode> response = restTemplate.postForEntity("https://api.cohere.ai/v1/chat", request, JsonNode.class);

            // Log raw response
            System.out.println("Cohere full raw response:");
            System.out.println(response.getBody().toPrettyString());

            String aiAnalysisText = response.getBody().has("text")
                    ? response.getBody().get("text").asText()
                    : response.getBody().get("generations").get(0).get("text").asText();

            // Log extracted text
            System.out.println("Cohere extracted 'text':");
            System.out.println(aiAnalysisText);

            // Clean markdown if any
            String cleanedJson = aiAnalysisText
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            System.out.println("Cleaned JSON:");
            System.out.println(cleanedJson);

            // Parse JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode parsed = mapper.readTree(cleanedJson);

            Map<String, Object> result = new HashMap<>();
            result.put("techData", parsed.has("techData") ? parsed.get("techData").asText() : "Neutral");
            result.put("newsData", parsed.has("newsData") ? parsed.get("newsData").asText() : "Neutral");
            result.put("aiAnalysis", parsed.has("aiAnalysis") ? parsed.get("aiAnalysis").asText() : "Neutral");
            result.put("recommendation", parsed.has("recommendation") ? parsed.get("recommendation").asText() : "HOLD");
            result.put("reasoning", parsed.has("reasoning") ? parsed.get("reasoning").asText() : "No reasoning provided.");

            return result;

        } catch (Exception e) {
            System.out.println("Error in AIAnalysisService:");
            e.printStackTrace();
            return Map.of("error", "Failed to generate AI analysis.");
        }
    }
}
