package com.alerts;

// Represents an alert
public class Alert {
    private String patientId;
    private String condition;
    private long timestamp;

    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getCondition() {
        return condition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Triggers an alert based on identified conditions and logs the alert details.
     *
     */
    public void triggerAlert() {
        System.out.println("Alert: " + condition + " for patient " + patientId + " at " + timestamp);
    }
}
