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
import java.util.ArrayList;
import java.util.List;

public class StepsTracker extends Tracker implements Savable {

    // removed local day/entryDate; reuse Tracker's date/day
    private int steps;

    private static final String STEPS_FILE = "steps.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    // Constructor that accepts a specific date
    public StepsTracker(LocalDate date, int steps) {
        super(date);
        this.steps = steps;
    }

    // Backwards-compatible constructor (only steps) - uses today's date
    public StepsTracker(int steps) {
        super();
        this.steps = steps;
    }

    public LocalDate getEntryDate() { return getDate(); } // inherited

    public int getSteps() { return steps; }

    @Override
    public String getDataAsString() {
        // CSV: date,day,steps
        return getDate().toString() + "," + getDay() + "," + steps;
    }

    @Override
    public void saveToFile() throws IOException {
        FileWriter fw = new FileWriter(STEPS_FILE, true);
        fw.write(getDataAsString() + System.lineSeparator());
        fw.close();
    }

    // Convenience static helper to log a day's steps and persist it (accepts ISO date)
    public static void logDaily(String dateIso, int steps) throws IOException {
        LocalDate d;
        try {
            d = LocalDate.parse(dateIso, DATE_FMT);
        } catch (DateTimeParseException ex) {
            // fallback to today
            d = LocalDate.now();
        }
        StepsTracker entry = new StepsTracker(d, steps);
        entry.saveToFile();
    }

    // Parse steps.txt into entries
    private static List<StepsEntry> readAllEntries() {
        List<StepsEntry> list = new ArrayList<>();
        File f = new File(STEPS_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        LocalDate d = LocalDate.parse(parts[0].trim(), DATE_FMT);
                        String day = parts[1].trim();
                        int s = Integer.parseInt(parts[2].trim());
                        list.add(new StepsEntry(d, day, s));
                    } catch (DateTimeParseException | NumberFormatException ex) {
                        // skip malformed line
                    }
                }
            }
        } catch (IOException e) {
            // ignore read errors
        }
        return list;
    }

    // Generate a weekly report starting from the given ISO date (inclusive).
    // Example startDateIso: "2025-11-10"
    public static String generateWeeklyReport(String startDateIso) {
        LocalDate start;
        try {
            start = LocalDate.parse(startDateIso, DATE_FMT);
        } catch (DateTimeParseException ex) {
            return "Invalid start date format. Use yyyy-MM-dd.";
        }

        List<StepsEntry> entries = readAllEntries();
        int total = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Weekly Steps Report: ").append(start.toString()).append(" to ").append(start.plusDays(6).toString()).append(System.lineSeparator());
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            int stepsForDay = 0;
            String dayName = d.getDayOfWeek().name();
            for (StepsEntry e : entries) {
                if (e.date.equals(d)) {
                    stepsForDay += e.steps;
                    dayName = e.day != null && !e.day.isEmpty() ? e.day : dayName;
                }
            }
            total += stepsForDay;
            sb.append(d.toString()).append(" (").append(dayName).append("): ").append(stepsForDay).append(" steps").append(System.lineSeparator());
        }
        // average over 7 days (missing days count as 0)
        double average = (double) total / 7.0;
        sb.append("Total steps: ").append(total).append(System.lineSeparator());
        sb.append(String.format("Average daily steps (over 7 days): %.2f", average)).append(System.lineSeparator());

        // Health message per requirement
        if (average < 7500.0) {
            sb.append("Advice: Move a bit more and be healthy.");
        } else {
            sb.append("Good job — keep it up!");
        }
        return sb.toString();
    }

    // Internal simple data holder
    private static class StepsEntry {
        LocalDate date;
        String day;
        int steps;
        StepsEntry(LocalDate date, String day, int steps) {
            this.date = date;
            this.day = day;
            this.steps = steps;
        }
    }
}
