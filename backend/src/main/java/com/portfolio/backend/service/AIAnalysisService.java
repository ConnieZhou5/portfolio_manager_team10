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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
            // Step 1: Fetch stock data (and extract company name from it)
            String techUrl = String.format("https://financialmodelingprep.com/api/v3/quote/%s?apikey=%s", symbol, fmpApiKey);
            JsonNode techData = restTemplate.getForObject(techUrl, JsonNode.class);

            if (techData == null || !techData.isArray() || techData.size() == 0) {
                return Map.of("error", "Stock data not found for symbol: " + symbol);
            }

            String companyName = techData.get(0).get("name").asText();

            // Step 2: Fetch news using company name
            String encodedCompanyName = URLEncoder.encode(companyName, StandardCharsets.UTF_8);
            String newsUrl = String.format("https://newsapi.org/v2/everything?q=%s&apiKey=%s", encodedCompanyName, newsApiKey);
            JsonNode newsData = restTemplate.getForObject(newsUrl, JsonNode.class);

            // Step 3: Build AI prompt
            String aiPrompt = String.format(
                """
                Based on the following stock data and news, provide an investment recommendation for the company %s (stock symbol %s).

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
                companyName, symbol,
                techData.toPrettyString(),
                newsData.get("articles").toPrettyString()
            );

            // Step 4: Call Cohere API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cohereApiKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", aiPrompt);
            payload.put("model", "command-r");
            payload.put("chat_history", new ArrayList<>());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity("https://api.cohere.ai/v1/chat", request, JsonNode.class);

            String aiAnalysisText = response.getBody().has("text")
                    ? response.getBody().get("text").asText()
                    : response.getBody().get("generations").get(0).get("text").asText();

            // Step 5: Clean markdown if any
            String cleanedJson = aiAnalysisText
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            // Step 6: Parse JSON
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
