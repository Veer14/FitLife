package fitlife.config;

/**
 * Protected configuration class for Gemini API credentials.
 * DO NOT expose API key in version control.
 * Store as environment variable: GEMINI_API_KEY
 */
public class GeminiConfig {
    
    private static final String MODEL_NAME = "gemini-2.0-flash-lite";
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    
    /**
     * Get the Gemini model name
     * @return model name string
     */
    public static String getModelName() {
        return MODEL_NAME;
    }
    
    /**
     * Get the Gemini API key from environment variable
     * @return API key string
     * @throws IllegalStateException if API key not set
     */
    public static String getApiKey() {
        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            throw new IllegalStateException(
                "GEMINI_API_KEY environment variable not set. " +
                "Please set it before running the application."
            );
        }
        return API_KEY;
    }
    
    /**
     * Validate that API key is available
     * @return true if API key is set, false otherwise
     */
    public static boolean isApiKeyConfigured() {
        return API_KEY != null && !API_KEY.trim().isEmpty();
    }
}
