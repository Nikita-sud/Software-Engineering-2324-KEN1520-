package com.cardio_generator.generators;

import java.util.Random;
import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generator for simulating blood pressure data for patients.
 */
public class BloodPressureDataGenerator implements PatientDataGenerator {

    // Constants should be in UPPER_SNAKE_CASE.
    private static final Random RANDOM = new Random();

    // Instance variables should be in camelCase.
    private int[] lastSystolicValues;
    private int[] lastDiastolicValues;

    /**
     * Constructor initializes baseline systolic and diastolic blood pressure values for each patient.
     *
     * @param patientCount the number of patients to initialize
     */
    public BloodPressureDataGenerator(int patientCount) {
        lastSystolicValues = new int[patientCount + 1]; // Includes an extra entry for convenience
        lastDiastolicValues = new int[patientCount + 1]; // Includes an extra entry for convenience

        // Initialize with baseline values for each patient.
        for (int i = 1; i <= patientCount; i++) {
            lastSystolicValues[i] = 110 + RANDOM.nextInt(20); // Random baseline between 110 and 130
            lastDiastolicValues[i] = 70 + RANDOM.nextInt(15); // Random baseline between 70 and 85
        }
    }

    /**
     * Generates blood pressure data for a patient and outputs it.
     *
     * @param patientId       the ID of the patient for whom to generate data
     * @param outputStrategy  the strategy to output the generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        // Validate patientId range.
        if (patientId < 1 || patientId >= lastSystolicValues.length) {
            System.err.println("Invalid patientId: " + patientId); // Prefer using a logging framework.
            return;
        }
        
        try {
            int systolicVariation = RANDOM.nextInt(5) - 2; // Random variation between -2 and 2
            int diastolicVariation = RANDOM.nextInt(5) - 2; // Random variation between -2 and 2
            int newSystolicValue = lastSystolicValues[patientId] + systolicVariation;
            int newDiastolicValue = lastDiastolicValues[patientId] + diastolicVariation;

            // Ensure the blood pressure stays within a realistic and safe range.
            newSystolicValue = Math.min(Math.max(newSystolicValue, 90), 180);
            newDiastolicValue = Math.min(Math.max(newDiastolicValue, 60), 120);

            // Update the last known values.
            lastSystolicValues[patientId] = newSystolicValue;
            lastDiastolicValues[patientId] = newDiastolicValue;

            // Use a single timestamp for both systolic and diastolic pressure to maintain consistency.
            long timestamp = System.currentTimeMillis();
            outputStrategy.output(patientId, timestamp, "SystolicPressure", Integer.toString(newSystolicValue));
            outputStrategy.output(patientId, timestamp, "DiastolicPressure", Integer.toString(newDiastolicValue));
        } catch (Exception e) {
            // Prefer using a logging framework over System.err.
            System.err.println("An error occurred while generating blood pressure data for patient " + patientId);
            e.printStackTrace(); // Helps identify where the error occurred.
        }
    }
}
