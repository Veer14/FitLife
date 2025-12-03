package fitlife.core;

import fitlife.data.Savable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;

public class WaterTracker extends Tracker implements Savable {

    // changed: store liters (double)
    private double liters;  // in liters

    private static final String WATER_FILE = "water.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    // Use today's date
    public WaterTracker(double liters) {
        super();
        this.liters = liters;
    }

    public double getWaterAmount() {
        return liters;
    }

    @Override
    public String getDataAsString() {
        // CSV: date,day,liters
        String dayName = getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return getDate().toString() + "," + dayName + "," + String.format(Locale.ROOT, "%.2f", liters);
    }

    @Override
    public void saveToFile() throws IOException {
        try (FileWriter fw = new FileWriter(WATER_FILE, true)) {
            fw.write(getDataAsString() + System.lineSeparator());
        }
    }

    // Log for a specific date (dateIso = "yyyy-MM-dd"), tolerant to old two-column format
    public static void logDaily(String dateIso, double liters) throws IOException {
        LocalDate d;
        try {
            d = LocalDate.parse(dateIso, DATE_FMT);
        } catch (DateTimeParseException ex) {
            d = LocalDate.now();
        }
        String dayName = d.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        try (FileWriter fw = new FileWriter(WATER_FILE, true)) {
            fw.write(d.toString() + "," + dayName + "," + String.format(Locale.ROOT, "%.2f", liters) + System.lineSeparator());
        }
    }

    // Generate a weekly report starting from startDateIso (inclusive). Average is over 7 days.
    public static String generateWeeklyReport(String startDateIso) {
        LocalDate start;
        try {
            start = LocalDate.parse(startDateIso, DATE_FMT);
        } catch (DateTimeParseException ex) {
            return "Invalid start date format. Use yyyy-MM-dd.";
        }

        double[] waterPerDay = new double[7]; // liters per day

        File f = new File(WATER_FILE);
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        try {
                            LocalDate d = LocalDate.parse(parts[0].trim(), DATE_FMT);
                            int idx = (int) java.time.Duration.between(start.atStartOfDay(), d.atStartOfDay()).toDays();
                            if (idx >= 0 && idx < 7) {
                                double val;
                                if (parts.length >= 3) {
                                    val = Double.parseDouble(parts[2].trim());
                                } else {
                                    // legacy format: date,amount (assume liters)
                                    val = Double.parseDouble(parts[1].trim());
                                }
                                waterPerDay[idx] += val;
                            }
                        } catch (Exception e) {
                            // skip malformed line
                        }
                    }
                }
            } catch (IOException e) {
                // ignore read errors
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Weekly Water Report: ").append(start.toString()).append(" to ").append(start.plusDays(6).toString()).append(System.lineSeparator());
        double total = 0.0;
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            sb.append(d.toString()).append(": ").append(String.format(Locale.ROOT, "%.2f", waterPerDay[i])).append(" L").append(System.lineSeparator());
            total += waterPerDay[i];
        }

        double average = total / 7.0;
        sb.append(String.format("Average daily water intake (over 7 days): %.2f L", average)).append(System.lineSeparator());

        if (average < 4.0) {
            sb.append("Advice: Drink more water to reach at least 4L per day.");
        } else {
            sb.append("Good job — your water intake is sufficient.");
        }

        return sb.toString();
    }

    // small dry-run main for quick testing (optional)
    public static void main(String[] args) {
        try {
            // sample logs (adjust dates as needed)
            logDaily("2025-11-10", 1.5);
            logDaily("2025-11-11", 0.5);
            logDaily("2025-11-12", 0.8);
            logDaily("2025-11-13", 1.0);
            logDaily("2025-11-14", 0.6);
            logDaily("2025-11-15", 1.2);
            logDaily("2025-11-16", 0.9);

            System.out.println(generateWeeklyReport("2025-11-10"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
