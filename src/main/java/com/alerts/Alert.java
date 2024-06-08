package com.alerts;

/**
 * Represents an alert.
 */
public class Alert {
    private String patientId;
    private String condition;
    private long timestamp;

    /**
     * Constructs an Alert object with the specified patient ID, condition, and timestamp.
     *
     * @param patientId the ID of the patient
     * @param condition the condition of the patient
     * @param timestamp the time the alert was generated
     */
    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    /**
     * Gets the ID of the patient.
     *
     * @return the patient ID
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Gets the condition of the patient.
     *
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Gets the time the alert was generated.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Triggers an alert based on identified conditions and logs the alert details.
     */
    public void triggerAlert() {
        System.out.println("Alert: " + condition + " for patient " + patientId + " at " + timestamp);
    }
}
