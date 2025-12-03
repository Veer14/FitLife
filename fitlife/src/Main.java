import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FitLifeGUI frame = new FitLifeGUI();
            frame.setVisible(true);
        });
    }
}