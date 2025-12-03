@echo off
javac -cp "lib\*" -d bin src\FitLifeGUI.java src\Main.java src\fitlife\config\GeminiConfig.java src\fitlife\ai\MetricsExtractor.java src\fitlife\ai\GeminiAnalyzer.java src\fitlife\core\CalorieTracker.java src\fitlife\core\StepsTracker.java src\fitlife\core\WaterTracker.java src\fitlife\core\Tracker.java src\fitlife\logic\BMITracker.java src\fitlife\logic\Calculable.java src\fitlife\data\Savable.java
echo Compilation complete
pause