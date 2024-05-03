package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates alerts based on the analysis of patient health data.
 * This class handles the evaluation of various health indicators such as blood pressure,
 * oxygen saturation, and ECG readings to detect conditions that may require immediate attention.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        if (patient != null) {
            evaluateBloodPressure(patient);
            evaluateBloodOxygen(patient);
            evaluateECGData(patient);
            evaluateHypotensiveHypoxemia(patient);
        } else {
            System.out.println("No patient data available for analysis.");
        }
    }

    private void evaluateHypotensiveHypoxemia(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> recentRecords = dataStorage.getRecords(patient.getPatientId(), currentTime - 600000, currentTime); // Last 10 minutes

        boolean lowBP = recentRecords.stream()
            .filter(r -> "SystolicPressure".equals(r.getRecordType()) && r.getMeasurementValue() < 90)
            .findFirst().isPresent();

        boolean lowSaturation = recentRecords.stream()
            .filter(r -> "Saturation".equals(r.getRecordType()) && r.getMeasurementValue() < 92)
            .findFirst().isPresent();

        if (lowBP && lowSaturation) {
            triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Hypotensive Hypoxemia Alert", currentTime));
        }
    }

    /**
     * Analyzes blood pressure records to detect critical conditions and trends for both systolic and diastolic pressures.
     *
     * @param patient The patient whose blood pressure is being monitored.
     */
    private void evaluateBloodPressure(Patient patient) {
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - 86400000;

        List<PatientRecord> systolicRecords = dataStorage.getRecords(patient.getPatientId(), oneDayAgo, currentTime)
            .stream()
            .filter(r -> "SystolicPressure".equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
            .limit(3)
            .collect(Collectors.toList());

        List<PatientRecord> diastolicRecords = dataStorage.getRecords(patient.getPatientId(), oneDayAgo, currentTime)
            .stream()
            .filter(r -> "DiastolicPressure".equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
            .limit(3)
            .collect(Collectors.toList());

        // Analyze trends for Systolic Pressure
        if (systolicRecords.size() == 3 && checkPressureTrend(systolicRecords, 10)) {
            triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Systolic Pressure Trend Alert", currentTime));
        }
        // Analyze trends for Diastolic Pressure
        if (diastolicRecords.size() == 3 && checkPressureTrend(diastolicRecords, 10)) {
            triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Diastolic Pressure Trend Alert", currentTime));
        }

        // Check for critical thresholds
        systolicRecords.forEach(record -> {
            if (record.getMeasurementValue() > 180 || record.getMeasurementValue() < 90) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Critical Systolic Pressure Alert", record.getTimestamp()));
            }
        });
        diastolicRecords.forEach(record -> {
            if (record.getMeasurementValue() > 120 || record.getMeasurementValue() < 60) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Critical Diastolic Pressure Alert", record.getTimestamp()));
            }
        });
    }

    /**
     * Checks for a consistent increase or decrease trend in measurement values across records.
     *
     * @param records   The list of patient records.
     * @param threshold The minimum difference to consider a trend consistent.
     * @return true if a consistent trend is found, false otherwise.
     */
    private boolean checkPressureTrend(List<PatientRecord> records, int threshold) {
        boolean increasing = true;
        boolean decreasing = true;
        for (int i = 0; i < records.size() - 1; i++) {
            increasing &= (records.get(i + 1).getMeasurementValue() - records.get(i).getMeasurementValue() > threshold);
            decreasing &= (records.get(i).getMeasurementValue() - records.get(i + 1).getMeasurementValue() > threshold);
        }
        return increasing || decreasing;
    }


    /**
     * Monitors oxygen saturation levels and alerts for rapid drops within a short period.
     *
     * @param patient The patient whose oxygen levels are being monitored.
     */
    private void evaluateBloodOxygen(Patient patient) {
        long currentTime = System.currentTimeMillis();
        long tenMinutesAgo = currentTime - 600000; // 10 minutes ago
        List<PatientRecord> boRecords = dataStorage.getRecords(patient.getPatientId(), tenMinutesAgo, currentTime)
            .stream()
            .filter(r -> "Saturation".equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
            .collect(Collectors.toList());
    
        if (!boRecords.isEmpty()) {
            // Check for low saturation alert
            boRecords.forEach(record -> {
                if (record.getMeasurementValue() < 92) {
                    triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Low Saturation Alert", record.getTimestamp()));
                }
            });
    
            // Check for rapid drop alert if there are at least two records
            if (boRecords.size() >= 2 && (boRecords.get(0).getMeasurementValue() - boRecords.get(1).getMeasurementValue() >= 5)) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Rapid Blood Oxygen Drop Alert", currentTime));
            }
        }
    }
    

    /**
     * Analyzes ECG data to identify abnormal heart rates and irregular beat patterns.
     *
     * @param patient The patient whose ECG data is being analyzed.
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

        // Check for irregular beats by comparing intervals
        PatientRecord previousRecord = ecgRecords.get(0);
        for (int i = 1; i < ecgRecords.size(); i++) {
            PatientRecord currentRecord = ecgRecords.get(i);
            long intervalDifference = Math.abs(currentRecord.getTimestamp() - previousRecord.getTimestamp());
            // Assuming 10% variation is significant - this threshold can be adjusted
            if (intervalDifference > 1000 * 0.1) { // Adjust this threshold based on actual criteria
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Irregular Beat Alert", currentRecord.getTimestamp()));
                break;
            }
            previousRecord = currentRecord;
        }
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("Alert triggered: " + alert.getCondition() + " for patient " + alert.getPatientId() + " at " + alert.getTimestamp());
    }
}
