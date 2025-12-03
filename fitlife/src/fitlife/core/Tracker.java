package fitlife.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.time.format.TextStyle;

public abstract class Tracker {
    protected LocalDate date;
    protected String day;

    // removed the incorrect getDay() method that caused infinite recursion

    public Tracker() {
        this.date = LocalDate.now();
        this.day = this.date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        // this.day initialization moved to constructor
    }

    // New ctor to allow creating entries for a specific date
    public Tracker(LocalDate date) {
        this.date = date;
        this.day = this.date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        // this.day initialization moved to constructor
    }

    public LocalDate getDate() {
        return date;
    }

    // Return the day name (e.g., "Monday")
    public String getDay() {
        return day;
    }

    // Every tracker will save data, so force them to implement this
    public abstract String getDataAsString();

    // Aggregate weekly summary across meals.txt, steps.txt, water.txt
    // startDateIso must be yyyy-MM-dd
    public static String generateWeeklySummary(String startDateIso) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate start;
        try {
            start = LocalDate.parse(startDateIso, fmt);
        } catch (DateTimeParseException ex) {
            return "Invalid start date format. Use yyyy-MM-dd.";
        }

        double[] calPerDay = new double[7];
        int[] stepsPerDay = new int[7];
        double[] waterPerDay = new double[7]; // liters per day

        // meals.txt expected format (new CalorieTracker): date,day,mealName,quantityGrams,calories,category
        File meals = new File("meals.txt");
        if (meals.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(meals))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 5) {
                        try {
                            LocalDate d = LocalDate.parse(p[0].trim(), fmt);
                            int idx = (int) java.time.Duration.between(start.atStartOfDay(), d.atStartOfDay()).toDays();
                            if (idx >= 0 && idx < 7) {
                                // calories expected at index 4 in the new format
                                double kcal = Double.parseDouble(p[4].trim());
                                calPerDay[idx] += kcal;
                            }
                        } catch (Exception e) {
                            // skip malformed line
                        }
                    }
                }
            } catch (Exception e) {
                // ignore read errors
            }
        }

        // steps.txt expected format: date,day,steps
        File steps = new File("steps.txt");
        if (steps.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(steps))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 3) {
                        try {
                            LocalDate d = LocalDate.parse(p[0].trim(), fmt);
                            int idx = (int) java.time.Duration.between(start.atStartOfDay(), d.atStartOfDay()).toDays();
                            if (idx >= 0 && idx < 7) {
                                int s = Integer.parseInt(p[2].trim());
                                stepsPerDay[idx] += s;
                            }
                        } catch (Exception e) {
                            // skip malformed line
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        // water.txt expected format: date,day,liters  (tolerant parsing)
        File water = new File("water.txt");
        if (water.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(water))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length >= 3) {
                        try {
                            LocalDate d = LocalDate.parse(p[0].trim(), fmt);
                            int idx = (int) java.time.Duration.between(start.atStartOfDay(), d.atStartOfDay()).toDays();
                            if (idx >= 0 && idx < 7) {
                                double liters = Double.parseDouble(p[2].trim());
                                waterPerDay[idx] += liters;
                            }
                        } catch (Exception e) {
                            // skip malformed line
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Weekly Summary ").append(start.toString()).append(" to ").append(start.plusDays(6).toString()).append(System.lineSeparator());
        int totalSteps = 0;
        double totalCal = 0.0;
        double totalWater = 0.0; // liters
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            sb.append(d.toString()).append(": calories=").append(String.format("%.2f", calPerDay[i]))
              .append(", steps=").append(stepsPerDay[i])
              .append(", water_liters=").append(String.format("%.2f", waterPerDay[i]))
              .append(System.lineSeparator());
            totalCal += calPerDay[i];
            totalSteps += stepsPerDay[i];
            totalWater += waterPerDay[i];
        }

        double avgCal = totalCal / 7.0;
        double avgSteps = (double) totalSteps / 7.0;
        double avgWaterLiters = totalWater / 7.0;

        sb.append("Averages (per day over 7 days):").append(System.lineSeparator());
        sb.append(String.format("Calories: %.2f kcal/day%n", avgCal));
        sb.append(String.format("Steps: %.2f steps/day%n", avgSteps));
        sb.append(String.format("Water: %.2f L/day%n", avgWaterLiters));

        // health messages
        if (avgCal > 0) {
            sb.append(avgCal > 2500 ? "Note: Your average calories are quite high. Consider dietary adjustments." : "Calorie intake looks reasonable.");
            sb.append(System.lineSeparator());
        } else {
            sb.append("No calorie data for the week.").append(System.lineSeparator());
        }

        if (avgSteps < 7500.0) {
            sb.append("Steps: Move a bit more and be healthy.").append(System.lineSeparator());
        } else {
            sb.append("Steps: Good job — keep it up!").append(System.lineSeparator());
        }

        if (avgWaterLiters < 4.0) {
            sb.append("Water: Try to drink more water daily (target ~4.0 L/day).");
        } else {
            sb.append("Water: Good hydration levels.");
        }

        return sb.toString();
    }
}
