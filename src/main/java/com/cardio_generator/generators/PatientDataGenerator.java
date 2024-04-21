package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for patient data generation.
 * Defines a method for generating data for a given patient.
 */
public interface PatientDataGenerator {

    /**
     * Generates data for a patient and outputs it using the given strategy.
     *
     * @param patientId       the unique identifier of the patient
     * @param outputStrategy  the strategy for outputting the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
