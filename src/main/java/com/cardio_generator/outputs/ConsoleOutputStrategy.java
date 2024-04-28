package com.cardio_generator.outputs;

/**
 * Strategy for outputting patient data to the console.
 */
public class ConsoleOutputStrategy implements OutputStrategy {

    /**
     * Outputs the provided patient data to the console.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time at which the data was recorded
     * @param label     the type of data (e.g., 'BloodPressure', 'HeartRate')
     * @param data      the actual data to output
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        System.out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
    }
}
