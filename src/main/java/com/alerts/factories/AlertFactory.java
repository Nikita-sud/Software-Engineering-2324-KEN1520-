package com.alerts.factories;

import com.alerts.Alert;

/**
 * Abstract factory class for creating Alert objects.
 */
public abstract class AlertFactory {
    
    /**
     * Abstract method to create an Alert object.
     * 
     * @param patientId the ID of the patient
     * @param condition the condition of the patient
     * @param timestamp the time the alert was generated
     * @return a new Alert object
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
