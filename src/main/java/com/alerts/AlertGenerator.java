package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the generation of medical alerts based on patient data analysis. This class evaluates various health
 * indicators such as blood pressure, oxygen saturation, and ECG readings to detect conditions requiring urgent attention.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Initializes a new AlertGenerator instance with a specific DataStorage.
     *
     * @param dataStorage the data storage system used to access patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates patient data to determine if any alert conditions are met.
     * If a condition is met, an alert is triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     * @throws NullPointerException if the patient object is null, indicating no patient data is available for analysis
     */
    public void evaluateData(Patient patient) {
        if (patient == null) {
            throw new NullPointerException("Patient data is null.");
        }
        evaluateBloodPressure(patient);
        evaluateBloodOxygen(patient);
        evaluateECGData(patient);
        evaluateHypotensiveHypoxemia(patient);
    }

    /**
     * Evaluates the patient's recent blood pressure and oxygen saturation to identify hypotensive hypoxemia conditions.
     *
     * @param patient the patient to evaluate
     */
    private void evaluateHypotensiveHypoxemia(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> recentRecords = dataStorage.getRecords(patient.getPatientId(), currentTime - 600000, currentTime);

        boolean lowBP = recentRecords.stream()
            .anyMatch(r -> "SystolicPressure".equals(r.getRecordType()) && r.getMeasurementValue() < 90);

        boolean lowSaturation = recentRecords.stream()
            .anyMatch(r -> "Saturation".equals(r.getRecordType()) && r.getMeasurementValue() < 92);

        if (lowBP && lowSaturation) {
            triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Hypotensive Hypoxemia Alert", currentTime));
        }
    }

    /**
     * Evaluates blood pressure records to detect any critical conditions or trends that require attention.
     *
     * @param patient the patient whose blood pressure is monitored
     */
    private void evaluateBloodPressure(Patient patient) {
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - 86400000;
    
        List<PatientRecord> systolicRecords = dataStorage.getRecords(patient.getPatientId(), oneDayAgo, currentTime)
            .stream()
            .filter(r -> "SystolicPressure".equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
            .collect(Collectors.toList());
    
        List<PatientRecord> diastolicRecords = dataStorage.getRecords(patient.getPatientId(), oneDayAgo, currentTime)
            .stream()
            .filter(r -> "DiastolicPressure".equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
            .collect(Collectors.toList());
    
        if (!systolicRecords.isEmpty()) {
            checkAndTriggerPressureAlerts(systolicRecords, "Systolic", currentTime, patient);
        }
        if (!diastolicRecords.isEmpty()) {
            checkAndTriggerPressureAlerts(diastolicRecords, "Diastolic", currentTime, patient);
        }
    }
    
    public void checkAndTriggerPressureAlerts(List<PatientRecord> records, String type, long currentTime, Patient patient) {
        // Check critical thresholds
        for (PatientRecord record : records) {
            if ((type.equals("Systolic") && (record.getMeasurementValue() > 180 || record.getMeasurementValue() < 90))) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Critical Pressure Threshold Alert (Systolic)", record.getTimestamp()));
            }
            if ((type.equals("Diastolic") && (record.getMeasurementValue() > 120 || record.getMeasurementValue() < 60))) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Critical Pressure Threshold Alert (Diastolic)", record.getTimestamp()));
            }
        }
    
        // Check trends
        if (records.size() >= 3) {
            boolean increasing = true;
            boolean decreasing = true;
            for (int i = 0; i < records.size() - 1; i++) {
                increasing &= (records.get(i).getMeasurementValue() - records.get(i + 1).getMeasurementValue() > 10);
                decreasing &= (records.get(i + 1).getMeasurementValue() - records.get(i).getMeasurementValue() > 10);
            }
    
            if (increasing) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), type + "Pressure Increasing Trend Alert", currentTime));
            }
            if (decreasing) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), type + "Pressure Decreasing Trend Alert", currentTime));
            }
        }
    }
    
    /**
     * Evaluates oxygen saturation data to detect critically low levels or rapid decreases that may indicate a respiratory issue.
     *
     * @param patient the patient whose oxygen saturation levels are being monitored
     */
    private void evaluateBloodOxygen(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = dataStorage.getRecords(patient.getPatientId(), currentTime - 600000, currentTime)
            .stream()
            .filter(r -> "Saturation".equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
            .collect(Collectors.toList());
    
        for (PatientRecord record : records) {
            if (record.getMeasurementValue() < 92) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Low Saturation Alert", record.getTimestamp()));
                break; // Ensure only the first applicable alert is triggered
            }
        }
    
        for (int i = 1; i < records.size(); i++) {
            double dropPercentage = 100.0 * (records.get(i - 1).getMeasurementValue() - records.get(i).getMeasurementValue()) / records.get(i - 1).getMeasurementValue();
            if (dropPercentage >= 5) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Rapid Blood Oxygen Drop Alert", records.get(i).getTimestamp()));
                break; // Ensure proper alert sequence
            }
        }
    }
    
    
    
    /**
     * Evaluates ECG data for abnormal heart rates or irregular beat patterns.
     *
     * @param patient the patient whose ECG data is being analyzed
     */
    private void evaluateECGData(Patient patient) {
        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - 3600000;
        List<PatientRecord> ecgRecords = dataStorage.getRecords(patient.getPatientId(), oneHourAgo, currentTime)
            .stream()
            .filter(r -> "ECG".equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
            .collect(Collectors.toList());
    
        if (ecgRecords.isEmpty()) {
            return;
        }
    
        // Check for abnormal heart rate
        for (PatientRecord record : ecgRecords) {
            if (record.getMeasurementValue() < 50 || record.getMeasurementValue() > 100) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Abnormal Heart Rate Alert", record.getTimestamp()));
            }
        }
    
        // Calculate the average interval and determine irregularities
        double averageInterval = calculateAverageInterval(ecgRecords);
        double allowableVariation = averageInterval * 0.1; // Allowing 10% variation
    
        PatientRecord previousRecord = ecgRecords.get(0);
        for (int i = 1; i < ecgRecords.size(); i++) {
            PatientRecord currentRecord = ecgRecords.get(i);
            long intervalDifference = Math.abs(currentRecord.getTimestamp() - previousRecord.getTimestamp());
    
            if (Math.abs(intervalDifference - averageInterval) > allowableVariation) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Irregular Beat Alert", currentRecord.getTimestamp()));
                break;
            }
            previousRecord = currentRecord;
        }
    }
    
    private double calculateAverageInterval(List<PatientRecord> ecgRecords) {
        long totalInterval = 0;
        for (int i = 1; i < ecgRecords.size(); i++) {
            totalInterval += (ecgRecords.get(i).getTimestamp() - ecgRecords.get(i - 1).getTimestamp());
        }
        return totalInterval / (double)(ecgRecords.size() - 1);
    }
    
    
    

    /**
     * Triggers an alert based on identified conditions and logs the alert details.
     *
     * @param alert the alert to be triggered
     */
    private void triggerAlert(Alert alert) {
        System.out.println("Alert triggered: " + alert.getCondition() + " for patient " + alert.getPatientId() + " at " + alert.getTimestamp());
    }
}
