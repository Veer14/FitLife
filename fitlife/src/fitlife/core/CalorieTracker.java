package fitlife.core;

import fitlife.data.Savable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class CalorieTracker extends Tracker implements Savable {

    private String mealName;
    private double quantityGrams;
    private int calories;
    private String category;

    private static final String FOODS_FILE = "foods.txt";
    private static final String MEALS_FILE = "meals.txt";
    private static final Map<String, Double> caloriesPerGramMap = new HashMap<>();

    // load known foods on class initialization
    static {
        loadFoods();
    }

    private static void loadFoods() {
        File f = new File(FOODS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String name = parts[0].trim().toLowerCase(Locale.ROOT);
                    try {
                        double cpg = Double.parseDouble(parts[1].trim());
                        caloriesPerGramMap.put(name, cpg);
                    } catch (NumberFormatException ex) {
                        // ignore malformed line
                    }
                }
            }
        } catch (IOException e) {
            // ignore load errors
        }
    }

    private static void saveFoods() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FOODS_FILE, false))) {
            for (Map.Entry<String, Double> e : caloriesPerGramMap.entrySet()) {
                pw.println(e.getKey() + "," + e.getValue());
            }
        }
    }

    // Constructor when calories are provided (first time or explicit)
    public CalorieTracker(String mealName, double quantityGrams, int calories, String category) {
        super();
        this.mealName = mealName;
        this.quantityGrams = quantityGrams;
        this.category = category;

        String key = (mealName == null) ? "" : mealName.toLowerCase(Locale.ROOT);
        if (calories > 0 && quantityGrams > 0) {
            this.calories = calories;
            double cpg = calories / quantityGrams;
            caloriesPerGramMap.put(key, cpg);
            try {
                saveFoods();
            } catch (IOException e) {
                // ignore save errors (could log)
            }
        } else {
            Double cpg = caloriesPerGramMap.get(key);
            if (cpg != null) {
                this.calories = (int) Math.round(cpg * quantityGrams);
            } else {
                this.calories = 0; // unknown until user provides calories
            }
        }
    }

    // Convenience constructor when calories are omitted (lookup only)
    public CalorieTracker(String mealName, double quantityGrams, String category) {
        this(mealName, quantityGrams, 0, category);
    }

    // New constructor allowing explicit date
    public CalorieTracker(LocalDate date, String mealName, double quantityGrams, int calories, String category) {
        super(date);
        this.mealName = mealName;
        this.quantityGrams = quantityGrams;
        this.category = category;

        String key = (mealName == null) ? "" : mealName.toLowerCase(Locale.ROOT);
        if (calories > 0 && quantityGrams > 0) {
            this.calories = calories;
            double cpg = calories / quantityGrams;
            caloriesPerGramMap.put(key, cpg);
            try {
                saveFoods();
            } catch (IOException e) {
                // ignore save errors (could log)
            }
        } else {
            Double cpg = caloriesPerGramMap.get(key);
            if (cpg != null) {
                this.calories = (int) Math.round(cpg * quantityGrams);
            } else {
                this.calories = 0; // unknown until user provides calories
            }
        }
    }

    // New constructor allowing explicit date with calories omitted -> delegate to 5-arg ctor
    public CalorieTracker(LocalDate date, String mealName, double quantityGrams, String category) {
        this(date, mealName, quantityGrams, 0, category);
    }

    public int getCalories() {
        return calories;
    }

    @Override
    public String getDataAsString() {
        // CSV: date,day,mealName,quantityGrams,calories,category
        String safeMeal = (mealName == null) ? "" : mealName;
        String safeCategory = (category == null) ? "" : category;
        return date.toString() + "," + getDay() + "," + safeMeal + "," + (int)Math.round(quantityGrams) + "," + calories + "," + safeCategory;
    }

    @Override
    public void saveToFile() throws IOException {
        FileWriter fw = new FileWriter(MEALS_FILE, true);
        fw.write(getDataAsString() + System.lineSeparator());
        fw.close();
    }
}
