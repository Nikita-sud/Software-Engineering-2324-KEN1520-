package com.cardio_generator.generators;

import java.util.Random;
import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generator for simulating blood saturation data for patients.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    
    // Constants should be in UPPER_SNAKE_CASE according to the style guide.
    private static final Random RANDOM = new Random();
    
    // Instance variables should be in camelCase according to the style guide.
    private int[] lastSaturationValues;

    /**
     * Constructor initializes baseline saturation values for each patient.
     *
     * @param patientCount the number of patients to initialize
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1]; // +1 to accommodate patient IDs starting from 1

        // Initialize with baseline saturation values for each patient.
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + RANDOM.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Generates blood saturation data for a patient and outputs it using the provided strategy.
     *
     * @param patientId       the ID of the patient for whom to generate data
     * @param outputStrategy  the strategy to output the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        // Check that the patientId is within the valid range.
        if (patientId < 1 || patientId >= lastSaturationValues.length) {
            // Logging should be used instead of System.err for production code.
            System.err.println("Invalid patientId: " + patientId);
            return;
        }
        
        try {
            // Simulate blood saturation values with small fluctuations.
            int variation = RANDOM.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range.
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;

            // Output saturation as a percentage string.
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Integer.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            // Consider using a logging framework instead of System.err.
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
