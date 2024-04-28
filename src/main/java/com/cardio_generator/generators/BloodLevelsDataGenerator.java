package com.cardio_generator.generators;

import java.util.Random;
import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generator for simulating blood levels data for patients.
 */

public class BloodLevelsDataGenerator implements PatientDataGenerator {

    // Constants should be in UPPER_SNAKE_CASE (Google Java Style Guide).
    private static final Random RANDOM = new Random();

    // Instance variables should be in camelCase (Google Java Style Guide).
    private final double[] baselineCholesterol; // Baseline cholesterol levels for patients
    private final double[] baselineWhiteCells; // Baseline white cell counts for patients
    private final double[] baselineRedCells; // Baseline red cell counts for patients

    /**
     * Initializes new generator with baseline values for each patient.
     *
     * @param patientCount the number of patients to generate baseline data for
     */
    public BloodLevelsDataGenerator(int patientCount) {
        // Initialize arrays to store baseline values for each patient.
        // The '+1' is to handle patient IDs starting from 1 instead of 0 (Google Java Style Guide).
        baselineCholesterol = new double[patientCount + 1];
        baselineWhiteCells = new double[patientCount + 1];
        baselineRedCells = new double[patientCount + 1];

        // Generate baseline values for each patient.
        for (int i = 1; i <= patientCount; i++) {
            baselineCholesterol[i] = 150 + RANDOM.nextDouble() * 50; // Initial random baseline
            baselineWhiteCells[i] = 4 + RANDOM.nextDouble() * 6; // Initial random baseline
            baselineRedCells[i] = 4.5 + RANDOM.nextDouble() * 1.5; // Initial random baseline
        }
    }

    /**
     * Generates simulated blood level data for a given patient and outputs it using the provided strategy.
     *
     * @param patientId the ID of the patient to generate data for
     * @param outputStrategy the output strategy to use for writing the data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        // It's better to check for valid patientId before proceeding (Google Java Style Guide).
        if (patientId < 1 || patientId >= baselineCholesterol.length) {
            // Use a logging framework instead of System.err.println (Google Java Style Guide).
            System.err.println("Invalid patientId: " + patientId);
            return;
        }
        
        try {
            // Generate values around the baseline for realism.
            // Local variable names should be in camelCase (Google Java Style Guide).
            double cholesterol = baselineCholesterol[patientId] + (RANDOM.nextDouble() - 0.5) * 10; 
            double whiteCells = baselineWhiteCells[patientId] + (RANDOM.nextDouble() - 0.5) * 1; 
            double redCells = baselineRedCells[patientId] + (RANDOM.nextDouble() - 0.5) * 0.2; 

            // Output the generated values. It's efficient to use one timestamp for all outputs.
            // Define a single timestamp for all outputs for consistency (Google Java Style Guide).
            long timestamp = System.currentTimeMillis();
            outputStrategy.output(patientId, timestamp, "Cholesterol", Double.toString(cholesterol));
            outputStrategy.output(patientId, timestamp, "WhiteBloodCells", Double.toString(whiteCells));
            outputStrategy.output(patientId, timestamp, "RedBloodCells", Double.toString(redCells));
        } catch (Exception e) {
            // Using a logging framework is preferred over printing to stderr (Google Java Style Guide).
            System.err.println("An error occurred while generating blood levels data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
