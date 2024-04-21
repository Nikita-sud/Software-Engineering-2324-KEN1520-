package com.cardio_generator.generators;

import java.util.Random;
import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generator for simulating ECG (electrocardiogram) data for patients.
 */
public class ECGDataGenerator implements PatientDataGenerator {

    // Random should be in UPPER_SNAKE_CASE since it's a constant.
    private static final Random RANDOM = new Random();

    // Instance variables should be in camelCase.
    private double[] lastEcgValues;

    // Constants should be in UPPER_SNAKE_CASE and 'Math.PI' is already a constant, so 'PI' is redundant.
    // Removed redundant declaration of PI since Math.PI is already a constant.

    /**
     * Constructor for ECGDataGenerator.
     *
     * @param patientCount the number of patients to initialize
     */
    public ECGDataGenerator(int patientCount) {
        lastEcgValues = new double[patientCount + 1];
        // Initialize the last ECG value for each patient to 0.
        for (int i = 1; i <= patientCount; i++) {
            lastEcgValues[i] = 0; // Initial ECG value can be set to 0
        }
    }

    /**
     * Generates and outputs ECG data for a specific patient.
     *
     * @param patientId       the ID of the patient for whom to generate data
     * @param outputStrategy  the strategy to use for outputting the data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        // Validate the patientId before proceeding with data generation.
        if (patientId < 1 || patientId >= lastEcgValues.length) {
            System.err.println("Invalid patientId: " + patientId); // Prefer using a logging framework.
            return;
        }

        try {
            double ecgValue = simulateEcgWaveform(patientId, lastEcgValues[patientId]);
            outputStrategy.output(patientId, System.currentTimeMillis(), "ECG", Double.toString(ecgValue));
            lastEcgValues[patientId] = ecgValue;
        } catch (Exception e) {
            System.err.println("An error occurred while generating ECG data for patient " + patientId);
            e.printStackTrace(); // Prefer using a logging framework for error handling.
        }
    }

    /**
     * Simulates an ECG waveform for a given patient.
     *
     * @param patientId      the ID of the patient
     * @param lastEcgValue   the last recorded ECG value for the patient
     * @return a simulated ECG waveform value
     */
    private double simulateEcgWaveform(int patientId, double lastEcgValue) {
        // Simplified ECG waveform generation based on sinusoids.
        double heartRate = 60.0 + RANDOM.nextDouble() * 20.0; // Heart rate variability between 60 and 80 bpm
        double currentTime = System.currentTimeMillis() / 1000.0; // Current time in seconds for the ECG signal
        double ecgFrequency = heartRate / 60.0; // ECG frequency in Hz based on heart rate

        // ECG components simulation using sinusoidal functions.
        double pWave = 0.1 * Math.sin(2 * Math.PI * ecgFrequency * currentTime);
        double qrsComplex = 0.5 * Math.sin(2 * Math.PI * 3 * ecgFrequency * currentTime); // QRS has a higher frequency
        double tWave = 0.2 * Math.sin(2 * Math.PI * 2 * ecgFrequency * currentTime + Math.PI / 4); // T wave offset

        // Add small random noise to simulate a more realistic ECG signal.
        return pWave + qrsComplex + tWave + RANDOM.nextDouble() * 0.05;
    }
}
