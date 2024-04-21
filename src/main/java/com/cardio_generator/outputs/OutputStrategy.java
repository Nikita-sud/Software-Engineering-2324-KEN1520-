package com.cardio_generator.outputs;

/**
 * Strategy interface for outputting data.
 * This can be implemented to output data in various ways such as to a console, file, or database.
 */
public interface OutputStrategy {

    /**
     * Outputs data associated with a patient.
     *
     * @param patientId the unique identifier for the patient
     * @param timestamp the time at which the data is recorded, expressed as a long value
     * @param label the label describing the type of data (e.g., "HeartRate")
     * @param data the actual data to output as a String
     */
    void output(int patientId, long timestamp, String label, String data);
}
