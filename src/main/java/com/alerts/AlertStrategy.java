package com.alerts;

import com.data_management.Patient;

/**
 * This interface defines a strategy for checking alerts related to a patient.
 * Implementations of this interface should provide specific alert checking mechanisms.
 */
public interface AlertStrategy {
    
    /**
     * Checks for alerts for the given patient.
     * Implementations of this method should define the specific criteria and actions
     * to be taken when an alert condition is met.
     *
     * @param patient the patient for whom alerts are to be checked
     */
    public void checkAlert(Patient patient);
}
