package fitlife.ai;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Extracts health metrics from FITLIFE data files
 */
public class MetricsExtractor {
    
    /**
     * Extract metrics from meals, steps, and water files for a date range
     * @param startDate start of analysis period
     * @param endDate end of analysis period
     * @return Map containing all extracted metrics
     */
    public static Map<String, Object> extractMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Extract calories from meals.txt
        Map<String, Object> mealMetrics = extractMealMetrics(startDate, endDate);
        metrics.putAll(mealMetrics);
        
        // Extract steps from steps.txt
        Map<String, Object> stepMetrics = extractStepMetrics(startDate, endDate);
        metrics.putAll(stepMetrics);
        
        // Extract water from water.txt
        Map<String, Object> waterMetrics = extractWaterMetrics(startDate, endDate);
        metrics.putAll(waterMetrics);
        
        // Add period info
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        metrics.put("analysis_period_days", daysBetween);
        metrics.put("analysis_start_date", startDate.toString());
        metrics.put("analysis_end_date", endDate.toString());
        
        return metrics;
    }
    
    /**
     * Extract calorie metrics from meals.txt
     */
    private static Map<String, Object> extractMealMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        int totalCalories = 0;
        Map<String, Integer> foodFrequency = new HashMap<>();
        int mealCount = 0;
        
        try {
            List<String> meals = Files.readAllLines(Paths.get("meals.txt"));
            for (String line : meals) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    try {
                        LocalDate mealDate = LocalDate.parse(parts[0]);
                        if (!mealDate.isBefore(startDate) && !mealDate.isAfter(endDate)) {
                            int calories = Integer.parseInt(parts[4]);
                            String foodName = parts[2];
                            
                            totalCalories += calories;
                            mealCount++;
                            foodFrequency.put(foodName, foodFrequency.getOrDefault(foodName, 0) + 1);
                        }
                    } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
                        // Skip malformed lines
                    }
                }
            }
        } catch (Exception e) {
            // meals.txt doesn't exist yet or read error
        }
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        result.put("average_daily_calories", mealCount > 0 ? (int) (totalCalories / daysBetween) : 0);
        result.put("total_calories_logged", totalCalories);
        result.put("total_meals_logged", mealCount);
        result.put("top_foods", getTopN(foodFrequency, 5));
        
        return result;
    }
    
    /**
     * Extract step metrics from steps.txt
     */
    private static Map<String, Object> extractStepMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        int totalSteps = 0;
        int stepDays = 0;
        int minSteps = Integer.MAX_VALUE;
        int maxSteps = 0;
        
        try {
            List<String> steps = Files.readAllLines(Paths.get("steps.txt"));
            for (String line : steps) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        LocalDate stepDate = LocalDate.parse(parts[0]);
                        if (!stepDate.isBefore(startDate) && !stepDate.isAfter(endDate)) {
                            int stepsValue = Integer.parseInt(parts[2]);
                            totalSteps += stepsValue;
                            stepDays++;
                            minSteps = Math.min(minSteps, stepsValue);
                            maxSteps = Math.max(maxSteps, stepsValue);
                        }
                    } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
                        // Skip malformed lines
                    }
                }
            }
        } catch (Exception e) {
            // steps.txt doesn't exist yet or read error
        }
        
        result.put("average_daily_steps", stepDays > 0 ? totalSteps / stepDays : 0);
        result.put("total_steps_logged", totalSteps);
        result.put("step_days_logged", stepDays);
        result.put("min_daily_steps", stepDays > 0 ? minSteps : 0);
        result.put("max_daily_steps", stepDays > 0 ? maxSteps : 0);
        
        return result;
    }
    
    /**
     * Extract water metrics from water.txt
     */
    private static Map<String, Object> extractWaterMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        double totalWater = 0;
        int waterDays = 0;
        double minWater = Double.MAX_VALUE;
        double maxWater = 0;
        
        try {
            List<String> water = Files.readAllLines(Paths.get("water.txt"));
            for (String line : water) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        LocalDate waterDate = LocalDate.parse(parts[0]);
                        if (!waterDate.isBefore(startDate) && !waterDate.isAfter(endDate)) {
                            double liters = Double.parseDouble(parts[parts.length - 1]);
                            totalWater += liters;
                            waterDays++;
                            minWater = Math.min(minWater, liters);
                            maxWater = Math.max(maxWater, liters);
                        }
                    } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
                        // Skip malformed lines
                    }
                }
            }
        } catch (Exception e) {
            // water.txt doesn't exist yet or read error
        }
        
        result.put("average_daily_water_liters", waterDays > 0 ? totalWater / waterDays : 0.0);
        result.put("total_water_logged_liters", totalWater);
        result.put("water_days_logged", (double) waterDays);
        result.put("min_daily_water_liters", waterDays > 0 ? minWater : 0.0);
        result.put("max_daily_water_liters", waterDays > 0 ? maxWater : 0.0);
        
        return result;
    }
    
    /**
     * Get top N items from frequency map
     */
    private static List<String> getTopN(Map<String, Integer> map, int n) {
        if (map.isEmpty()) {
            return new ArrayList<>();
        }
        return map.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(n)
            .map(Map.Entry::getKey)
            .toList();
    }
}