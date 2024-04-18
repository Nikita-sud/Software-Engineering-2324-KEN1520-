package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates alert data for patients based on predefined probabilities.
 * This class simulates alert conditions for monitoring systems.
 */
public class AlertGenerator implements PatientDataGenerator {

    // Changed to UPPER_SNAKE_CASE to indicate that it's a constant
    public static final Random RANDOM_GENERATOR = new Random();

    // Changed variable name to camelCase to adhere to Java variable naming conventions
    private boolean[] alertStates; // false = resolved, true = triggered

    /**
     * Initializes alert states for each patient.
     * @param patientCount the number of patients to monitor
     */
    public AlertGenerator(int patientCount) {
        // Changed variable name to camelCase
        alertStates = new boolean[patientCount + 1]; // Ensures array size accounts for all patient IDs
    }

    /**
     * Generates either a resolved or triggered alert based on current state and probability.
     * @param patientId the patient identifier
     * @param outputStrategy the strategy to output alert data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                // Reduced the use of magic numbers by defining them as static constants
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Improved variable names for clarity
                double alertRate = 0.1; // Adjusted for desired frequency of alerts
                double alertProbability = -Math.expm1(-alertRate); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < alertProbability;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Specific exception handling for out-of-bounds access
            System.err.println("Invalid patient ID: " + patientId);
        } catch (Exception e) {
            // General exception handling, discouraged but used here for unexpected errors
            System.err.println("An unexpected error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
