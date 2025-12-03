package fitlife.logic;

import java.util.Scanner;
import java.text.DecimalFormat;

public class BMITracker implements Calculable {

    private double height; // in meters
    private double weight; // in kg
    private int age;      // in years

    public BMITracker(double height, double weight) {
        this(0, height, weight);
    }

    public BMITracker(int age, double height, double weight) {
        this.age = age;
        this.height = height;
        this.weight = weight;
    }

    @Override
    public double calculate() {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative.");
        }
        if (height <= 0 || weight <= 0) {
            throw new IllegalArgumentException("Height and weight must be positive values.");
        }
        return weight / (height * height);
    }

    public String getBMICategory() {
        double bmiValue = calculate();

        if (bmiValue < 18.5)
            return "Underweight";
        else if (bmiValue < 25)
            return "Normal";
        else if (bmiValue < 30)
            return "Overweight";
        else
            return "Obese";
    }

    // simple CLI for quick use
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.print("Enter age (years): ");
            int age = sc.nextInt();
            System.out.print("Enter height (meters): ");
            double height = sc.nextDouble();
            System.out.print("Enter weight (kg): ");
            double weight = sc.nextDouble();

            BMITracker tracker = new BMITracker(age, height, weight);
            double bmi = tracker.calculate();
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println("BMI: " + df.format(bmi));
            System.out.println("Category: " + tracker.getBMICategory());
        } catch (IllegalArgumentException ex) {
            System.err.println("Input error: " + ex.getMessage());
        }
    }
}
