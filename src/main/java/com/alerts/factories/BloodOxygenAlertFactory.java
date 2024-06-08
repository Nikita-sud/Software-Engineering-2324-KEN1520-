package com.alerts.factories;

import com.alerts.Alert;

/**
 * Concrete factory class for creating blood oxygen level alerts.
 */
public class BloodOxygenAlertFactory extends AlertFactory {
    
    /**
     * Creates an Alert object for blood oxygen level conditions.
     *
     * @param patientId the ID of the patient
     * @param condition the condition of the patient
     * @param timestamp the time the alert was generated
     * @return a new Alert object for the specified condition
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, condition, timestamp);
    }
}
