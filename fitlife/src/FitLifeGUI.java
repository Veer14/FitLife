import fitlife.core.CalorieTracker;
import fitlife.core.StepsTracker;
import fitlife.core.WaterTracker;
import fitlife.core.Tracker;
import fitlife.logic.BMITracker;
import fitlife.config.GeminiConfig;
import fitlife.ai.MetricsExtractor;
import fitlife.ai.GeminiAnalyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FitLifeGUI extends JFrame {
    private JPanel dashboardPanel;
    private JScrollPane dashboardScroll;

    public FitLifeGUI() {
        setTitle("FitLife - AI-Powered Health Tracker");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // Main container with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(245, 245, 250));

        // Top header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Middle content - Dashboard and Buttons side by side
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(new Color(245, 245, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Left side - Dashboard
        dashboardPanel = createDashboardPanel();
        dashboardScroll = new JScrollPane(dashboardPanel);
        dashboardScroll.setPreferredSize(new Dimension(400, 600));
        contentPanel.add(dashboardScroll, BorderLayout.CENTER);

        // Right side - Buttons
        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setBackground(new Color(41, 128, 185)); // Modern blue
        header.setPreferredSize(new Dimension(900, 80));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("🏥 FitLife - Your AI Health Companion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Track your health, get AI insights");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(220, 240, 255));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setBackground(new Color(41, 128, 185));
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel();
        dashboard.setLayout(new BoxLayout(dashboard, BoxLayout.Y_AXIS));
        dashboard.setBackground(Color.WHITE);
        dashboard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Today's Date
        JLabel dateLabel = new JLabel("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dashboard.add(dateLabel);
        dashboard.add(Box.createVerticalStrut(15));

        // Meals Today
        dashboard.add(createDashboardCard("🍽️ Meals Today", getTodaysMeals()));
        dashboard.add(Box.createVerticalStrut(10));

        // Steps Today
        dashboard.add(createDashboardCard("👟 Steps Today", getTodaysSteps()));
        dashboard.add(Box.createVerticalStrut(10));

        // Water Today
        dashboard.add(createDashboardCard("💧 Water Today", getTodaysWater()));
        dashboard.add(Box.createVerticalStrut(10));

        // Recent Activity
        dashboard.add(createDashboardCard("📊 Recent Activity", getRecentActivity()));

        dashboard.add(Box.createVerticalGlue());

        return dashboard;
    }

    private JPanel createDashboardCard(String title, String content) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(new Color(255, 255, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 230), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(350, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(41, 128, 185));

        JTextArea contentArea = new JTextArea(content);
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        contentArea.setBackground(new Color(250, 250, 255));
        contentArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        contentArea.setForeground(new Color(50, 50, 50));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        return card;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(7, 1, 0, 10));
        buttonPanel.setBackground(new Color(245, 245, 250));
        buttonPanel.setPreferredSize(new Dimension(200, 0));

        Color btnColor = new Color(41, 128, 185);
        Color hoverColor = new Color(30, 100, 160);

        JButton mealBtn = createStyledButton("📝 Log Meal", btnColor, hoverColor);
        JButton stepsBtn = createStyledButton("👟 Log Steps", btnColor, hoverColor);
        JButton waterBtn = createStyledButton("💧 Log Water", btnColor, hoverColor);
        JButton bmiBtn = createStyledButton("⚖️ BMI Calculator", btnColor, hoverColor);
        JButton weeklyBtn = createStyledButton("📊 Weekly Summary", btnColor, hoverColor);
        JButton aiBtn = createStyledButton("🤖 AI Analysis", btnColor, hoverColor);
        JButton exitBtn = createStyledButton("❌ Exit", new Color(231, 76, 60), new Color(192, 57, 43));

        mealBtn.addActionListener(e -> { logMeal(); refreshDashboard(); });
        stepsBtn.addActionListener(e -> { logSteps(); refreshDashboard(); });
        waterBtn.addActionListener(e -> { logWater(); refreshDashboard(); });
        bmiBtn.addActionListener(e -> runBMI());
        weeklyBtn.addActionListener(e -> showWeeklySummary());
        aiBtn.addActionListener(e -> analyzeWithAI());
        exitBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(mealBtn);
        buttonPanel.add(stepsBtn);
        buttonPanel.add(waterBtn);
        buttonPanel.add(bmiBtn);
        buttonPanel.add(weeklyBtn);
        buttonPanel.add(aiBtn);
        buttonPanel.add(exitBtn);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void refreshDashboard() {
        // Recreate the dashboard panel
        dashboardPanel = createDashboardPanel();
        // Update the scroll pane's viewport with the new panel
        dashboardScroll.setViewportView(dashboardPanel);
        // Repaint the scroll pane
        dashboardScroll.repaint();
    }

    private String getTodaysMeals() {
        try {
            String today = LocalDate.now().toString();
            List<String> meals = Files.readAllLines(Paths.get("meals.txt"));
            List<String> todaysMeals = meals.stream()
                    .filter(line -> line.contains(today))
                    .collect(Collectors.toList());

            if (todaysMeals.isEmpty()) {
                return "No meals logged yet.\n\nTip: Log a meal to start tracking!";
            }
            return String.join("\n", todaysMeals.stream().limit(5).collect(Collectors.toList()));
        } catch (Exception e) {
            return "No data available";
        }
    }

    private String getTodaysSteps() {
        try {
            String today = LocalDate.now().toString();
            List<String> steps = Files.readAllLines(Paths.get("steps.txt"));
            List<String> todaysSteps = steps.stream()
                    .filter(line -> line.contains(today))
                    .collect(Collectors.toList());

            if (todaysSteps.isEmpty()) {
                return "No steps logged yet.\n\nDaily goal: 10,000 steps";
            }
            return String.join("\n", todaysSteps);
        } catch (Exception e) {
            return "No data available";
        }
    }

    private String getTodaysWater() {
        try {
            String today = LocalDate.now().toString();
            List<String> water = Files.readAllLines(Paths.get("water.txt"));
            List<String> todaysWater = water.stream()
                    .filter(line -> line.contains(today))
                    .collect(Collectors.toList());

            if (todaysWater.isEmpty()) {
                return "No water logged yet.\n\nDaily goal: 2-3 liters";
            }
            return String.join("\n", todaysWater);
        } catch (Exception e) {
            return "No data available";
        }
    }

    private String getRecentActivity() {
        StringBuilder activity = new StringBuilder();
        try {
            // Last 3 meals
            List<String> meals = Files.readAllLines(Paths.get("meals.txt"));
            activity.append("Recent meals:\n");
            meals.stream().skip(Math.max(0, meals.size() - 2)).forEach(m -> activity.append("  • ").append(m).append("\n"));

            // Last 3 steps entries
            List<String> steps = Files.readAllLines(Paths.get("steps.txt"));
            activity.append("\nRecent steps:\n");
            steps.stream().skip(Math.max(0, steps.size() - 2)).forEach(s -> activity.append("  • ").append(s).append("\n"));

            return activity.toString();
        } catch (Exception e) {
            return "No recent activity";
        }
    }

    private void logMeal() {
        JTextField dateField = new JTextField();
        JTextField mealField = new JTextField();
        JTextField qtyField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField caloriesField = new JTextField();

        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.add(new JLabel("Date (yyyy-MM-dd, empty = today):"));
        p.add(dateField);
        p.add(new JLabel("Meal name:"));
        p.add(mealField);
        p.add(new JLabel("Category:"));
        p.add(categoryField);
        p.add(new JLabel("Quantity (grams):"));
        p.add(qtyField);
        p.add(new JLabel("Calories (optional):"));
        p.add(caloriesField);

        int res = JOptionPane.showConfirmDialog(this, p, "Log Meal",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String date = dateField.getText().trim();
        String name = mealField.getText().trim();
        String category = categoryField.getText().trim();
        String qty = qtyField.getText().trim();
        String cal = caloriesField.getText().trim();

        if (name.isEmpty() || qty.isEmpty()) {
            showError("Meal name and quantity are required.");
            return;
        }

        try {
            double grams = Double.parseDouble(qty);
            CalorieTracker ct;

            if (!date.isEmpty()) {
                LocalDate d = LocalDate.parse(date);
                if (!cal.isEmpty()) {
                    ct = new CalorieTracker(d, name, grams, Integer.parseInt(cal), category);
                } else {
                    ct = new CalorieTracker(d, name, grams, category);
                }
            } else {
                if (!cal.isEmpty()) {
                    ct = new CalorieTracker(name, grams, Integer.parseInt(cal), category);
                } else {
                    ct = new CalorieTracker(name, grams, category);
                }
            }

            ct.saveToFile();
            showInfo("✅ Meal logged successfully.");

        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void logSteps() {
        JTextField dateField = new JTextField();
        JTextField stepsField = new JTextField();

        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.add(new JLabel("Date (yyyy-MM-dd, empty = today):"));
        p.add(dateField);
        p.add(new JLabel("Steps:"));
        p.add(stepsField);

        int res = JOptionPane.showConfirmDialog(this, p, "Log Steps",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            int steps = Integer.parseInt(stepsField.getText().trim());
            String date = dateField.getText().trim();

            if (date.isEmpty()) date = LocalDate.now().toString();

            StepsTracker.logDaily(date, steps);
            showInfo("✅ Steps logged successfully.");

        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void logWater() {
        JTextField dateField = new JTextField();
        JTextField litersField = new JTextField();

        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.add(new JLabel("Date (yyyy-MM-dd, empty = today):"));
        p.add(dateField);
        p.add(new JLabel("Liters (e.g., 1.5):"));
        p.add(litersField);

        int res = JOptionPane.showConfirmDialog(this, p, "Log Water",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            double liters = Double.parseDouble(litersField.getText().trim());
            String date = dateField.getText().trim();

            if (date.isEmpty()) date = LocalDate.now().toString();

            WaterTracker.logDaily(date, liters);
            showInfo("✅ Water logged successfully.");

        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void runBMI() {
        JTextField ageField = new JTextField();
        JTextField heightField = new JTextField();
        JTextField weightField = new JTextField();

        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.add(new JLabel("Age:"));
        p.add(ageField);
        p.add(new JLabel("Height (m):"));
        p.add(heightField);
        p.add(new JLabel("Weight (kg):"));
        p.add(weightField);

        int res = JOptionPane.showConfirmDialog(this, p, "BMI Calculator",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            int age = Integer.parseInt(ageField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());

            BMITracker bmi = new BMITracker(age, height, weight);
            double value = bmi.calculate();

            showInfo("BMI: " + String.format("%.2f", value)
                    + "\nCategory: " + bmi.getBMICategory());

        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }

    private void showWeeklySummary() {
        String start = JOptionPane.showInputDialog(this,
                "Start date (yyyy-MM-dd):", LocalDate.now().toString());

        if (start == null || start.trim().isEmpty()) return;

        String result = Tracker.generateWeeklySummary(start.trim());
        JTextArea area = new JTextArea(result);
        area.setEditable(false);
        area.setCaretPosition(0);
        area.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(550, 400));

        JOptionPane.showMessageDialog(this, scroll,
                "Weekly Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * NEW: AI Health Analysis with Gemini LLM
     */
    private void analyzeWithAI() {
        // Check if API key is configured
        if (!GeminiConfig.isApiKeyConfigured()) {
            showError("API Configuration Error:\n\n" +
                    "GEMINI_API_KEY environment variable is not set.\n\n" +
                    "Please set it before using AI analysis:\n\n" +
                    "Windows: setx GEMINI_API_KEY \"your-api-key\"\n" +
                    "Mac/Linux: export GEMINI_API_KEY=\"your-api-key\"\n\n" +
                    "Get free API key at: https://aistudio.google.com/app/apikeys");
            return;
        }

        // Create input dialog
        JTextArea queryArea = new JTextArea(3, 40);
        queryArea.setLineWrap(true);
        queryArea.setWrapStyleWord(true);
        queryArea.setText("What would you like to know about your health?");

        JScrollPane queryScroll = new JScrollPane(queryArea);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.add(new JLabel("Ask a specific health question:"), BorderLayout.NORTH);
        p.add(queryScroll, BorderLayout.CENTER);
        p.add(new JLabel("(e.g., 'Should I eat more protein?', 'Am I active enough?', 'Why am I tired?')"), BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(this, p, "🤖 AI Health Analysis",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res != JOptionPane.OK_OPTION) return;

        String userQuery = queryArea.getText().trim();
        if (userQuery.isEmpty() || userQuery.equals("What would you like to know about your health?")) {
            showError("Please enter a specific question");
            return;
        }

        // Show loading dialog
        JDialog loadingDialog = new JDialog(this, "Analyzing...", true);
        JLabel loadingLabel = new JLabel(
                "<html><center>" +
                "🤖 Calling Gemini AI for personalized analysis...<br>" +
                "This may take 3-5 seconds...<br><br>" +
                "Analyzing your health metrics...<br>" +
                "</center></html>"
        );
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingDialog.add(loadingLabel, BorderLayout.CENTER);
        loadingDialog.setSize(450, 180);
        loadingDialog.setLocationRelativeTo(this);

        // Run analysis in background thread
        new Thread(() -> {
            try {
                // Extract metrics (last 30 days)
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(29);
                Map<String, Object> metrics = MetricsExtractor.extractMetrics(startDate, endDate);

                // Call Gemini
                Map<String, Object> analysis = GeminiAnalyzer.analyzeUserHealth(metrics, userQuery);

                // Close loading dialog and display results
                loadingDialog.dispose();
                displayAnalysisResults(analysis, userQuery);

            } catch (Exception ex) {
                loadingDialog.dispose();
                showError("Analysis failed:\n" + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();

        loadingDialog.setVisible(true);
    }

    /**
     * Display AI analysis results in a formatted dialog
     */
    private void displayAnalysisResults(Map<String, Object> analysis, String userQuery) {
        // Check for errors
        if (analysis.containsKey("error")) {
            showError("Analysis Error:\n" + analysis.get("error"));
            return;
        }

        StringBuilder result = new StringBuilder();
        result.append("═══════════════════════════════════════════════════════\n");
        result.append("             🤖 AI HEALTH ANALYSIS RESULTS              \n");
        result.append("═══════════════════════════════════════════════════════\n\n");

        result.append("YOUR QUESTION:\n");
        result.append("─────────────────────────────────────────────────────\n");
        result.append("\"").append(userQuery).append("\"\n\n");

        result.append("ANSWER:\n");
        result.append("─────────────────────────────────────────────────────\n");
        result.append(analysis.getOrDefault("answer", "N/A")).append("\n\n");

        result.append("KEY INSIGHTS:\n");
        result.append("─────────────────────────────────────────────────────\n");
        @SuppressWarnings("unchecked")
        List<String> insights = (List<String>) analysis.getOrDefault("insights", new java.util.ArrayList<>());
        if (insights.isEmpty()) {
            result.append("• No additional insights\n");
        } else {
            for (String insight : insights) {
                result.append("• ").append(insight).append("\n");
            }
        }
        result.append("\n");

        result.append("RECOMMENDATIONS:\n");
        result.append("─────────────────────────────────────────────────────\n");
        @SuppressWarnings("unchecked")
        List<String> recommendations = (List<String>) analysis.getOrDefault("recommendations", new java.util.ArrayList<>());
        if (recommendations.isEmpty()) {
            result.append("• No specific recommendations\n");
        } else {
            for (String rec : recommendations) {
                result.append("• ").append(rec).append("\n");
            }
        }
        result.append("\n");

        double confidence = ((Number) analysis.getOrDefault("confidence", 0.0)).doubleValue();
        result.append("─────────────────────────────────────────────────────\n");
        result.append("Analysis Confidence: ").append(String.format("%.0f%%", confidence * 100)).append("\n");
        result.append("═══════════════════════════════════════════════════════\n");

        JTextArea area = new JTextArea(result.toString());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 11));
        area.setCaretPosition(0);
        area.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 500));

        JOptionPane.showMessageDialog(this, scroll, "🤖 AI Analysis Results", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "✅ Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "❌ Error",
                JOptionPane.ERROR_MESSAGE);
    }
}