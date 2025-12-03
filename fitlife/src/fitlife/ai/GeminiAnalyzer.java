package fitlife.ai;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fitlife.config.GeminiConfig;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Calls Google Gemini API via REST for personalized health analysis
 */
public class GeminiAnalyzer {
    
    private static final Gson gson = new Gson();
    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent";
    
    /**
     * Analyze user health data with Gemini LLM
     * @param metrics extracted metrics from data files
     * @param userQuery user's specific question
     * @return Map containing analysis results
     */
    public static Map<String, Object> analyzeUserHealth(Map<String, Object> metrics, String userQuery) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate API key
            if (!GeminiConfig.isApiKeyConfigured()) {
                result.put("error", "API key not configured. Set GEMINI_API_KEY environment variable.");
                result.put("confidence", 0.0);
                return result;
            }
            
            // Build prompt
            String userPrompt = buildDetailedPrompt(metrics, userQuery);
            
            // Call Gemini API via REST
            String apiResponse = callGeminiAPI(userPrompt, GeminiConfig.getApiKey());
            
            // Extract JSON from response (may be wrapped in markdown code blocks)
            String jsonResponse = extractJsonFromResponse(apiResponse);
            
            // Parse response
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> analysis = gson.fromJson(jsonResponse, Map.class);
                
                // Ensure all expected fields exist
                if (!analysis.containsKey("answer")) {
                    analysis.put("answer", "Analysis complete");
                }
                if (!analysis.containsKey("insights")) {
                    analysis.put("insights", new java.util.ArrayList<String>());
                }
                if (!analysis.containsKey("recommendations")) {
                    analysis.put("recommendations", new java.util.ArrayList<String>());
                }
                if (!analysis.containsKey("confidence")) {
                    analysis.put("confidence", 0.85);
                }
                
                return analysis;
                
            } catch (JsonSyntaxException e) {
                // If JSON parsing fails, return raw response
                result.put("answer", apiResponse);
                result.put("insights", new java.util.ArrayList<String>());
                result.put("recommendations", new java.util.ArrayList<String>());
                result.put("confidence", 0.70);
                return result;
            }
            
        } catch (Exception e) {
            result.put("error", "API Error: " + e.getMessage());
            result.put("confidence", 0.0);
            return result;
        }
    }
    
    /**
     * Extract JSON from response (handles markdown code blocks)
     */
    private static String extractJsonFromResponse(String response) {
        // If response contains ```json ... ```, extract the JSON
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        // If response contains ```...```, extract the content
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        // Otherwise return as is
        return response.trim();
    }
    
    /**
     * Call Gemini API via REST HTTP
     */
    private static String callGeminiAPI(String prompt, String apiKey) throws Exception {
        String url = API_ENDPOINT + "?key=" + apiKey;
        
        // Build request payload
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new java.util.ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, String>> parts = new java.util.ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);
        
        String jsonPayload = gson.toJson(requestBody);
        
        // Make HTTP request
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        
        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("API returned status code: " + responseCode);
        }
        
        Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        
        // Extract text from response
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = gson.fromJson(response, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
        
        if (candidates != null && !candidates.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> content2 = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts2 = (List<Map<String, Object>>) content2.get("parts");
            if (parts2 != null && !parts2.isEmpty()) {
                return (String) parts2.get(0).get("text");
            }
        }
        
        throw new Exception("Invalid API response format");
    }
    
    /**
     * Build detailed prompt for Gemini with metrics and user query
     */
    private static String buildDetailedPrompt(Map<String, Object> metrics, String userQuery) {
        @SuppressWarnings("unchecked")
        List<String> topFoods = (List<String>) metrics.getOrDefault("top_foods", new java.util.ArrayList<>());
        
        return String.format("""
            You are an expert personal health and fitness analyst. Analyze the following user health data and answer their specific question.
            
            === USER'S HEALTH DATA ===
            Analysis Period: %s to %s (%d days)
            
            NUTRITION:
            - Average Daily Calories: %d kcal
            - Total Meals Logged: %d meals
            - Top Foods Eaten: %s
            
            ACTIVITY:
            - Average Daily Steps: %d steps
            - Days With Activity Logged: %d days
            - Step Range: %d - %d steps per day
            
            HYDRATION:
            - Average Daily Water: %.1f liters
            - Days With Water Logged: %.0f days
            - Water Range: %.1f - %.1f liters per day
            
            === USER'S SPECIFIC QUESTION ===
            "%s"
            
            === YOUR RESPONSE ===
            Provide ONLY a valid JSON response with NO markdown formatting or extra text.
            Use this exact structure:
            {
              "answer": "Direct answer to their question (2-3 sentences)",
              "insights": ["insight 1", "insight 2", "insight 3"],
              "recommendations": ["recommendation 1", "recommendation 2", "recommendation 3"],
              "confidence": 0.85
            }
            
            Make insights and recommendations specific and actionable based on their data.
            Confidence should be 0.0-1.0 indicating how confident you are in this analysis.
            """,
            metrics.getOrDefault("analysis_start_date", "N/A"),
            metrics.getOrDefault("analysis_end_date", "N/A"),
            metrics.getOrDefault("analysis_period_days", 0),
            metrics.getOrDefault("average_daily_calories", 0),
            metrics.getOrDefault("total_meals_logged", 0),
            topFoods.isEmpty() ? "None logged yet" : String.join(", ", topFoods),
            metrics.getOrDefault("average_daily_steps", 0),
            metrics.getOrDefault("step_days_logged", 0),
            metrics.getOrDefault("min_daily_steps", 0),
            metrics.getOrDefault("max_daily_steps", 0),
            metrics.getOrDefault("average_daily_water_liters", 0.0),
            metrics.getOrDefault("water_days_logged", 0.0),
            metrics.getOrDefault("min_daily_water_liters", 0.0),
            metrics.getOrDefault("max_daily_water_liters", 0.0),
            userQuery
        );
    }
}