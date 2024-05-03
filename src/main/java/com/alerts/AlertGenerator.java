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
        } else {
            System.out.println("No patient data available for analysis.");
        }
    }

    /**
     * Analyzes blood pressure records to detect critical conditions and trends.
     *
     * @param patient The patient whose blood pressure is being monitored.
     */
    private void evaluateBloodPressure(Patient patient) {
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - 86400000;
        List<PatientRecord> bpRecords = dataStorage.getRecords(patient.getPatientId(), oneDayAgo, currentTime)
                .stream()
                .filter(r -> "BloodPressure".equals(r.getRecordType()))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
                .limit(3)
                .collect(Collectors.toList());

        if (bpRecords.size() == 3) {
            if (checkIncreaseTrend(bpRecords, 10) || checkDecreaseTrend(bpRecords, 10)) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Blood Pressure Trend Alert", currentTime));
            }
            if (checkThreshold(bpRecords, 180, 90)) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Critical Blood Pressure Alert", currentTime));
            }
        }
    }

    /**
     * Monitors oxygen saturation levels and alerts for rapid drops within a short period.
     *
     * @param patient The patient whose oxygen levels are being monitored.
     */
    private void evaluateBloodOxygen(Patient patient) {
        long currentTime = System.currentTimeMillis();
        long tenMinutesAgo = currentTime - 600000;
        List<PatientRecord> boRecords = dataStorage.getRecords(patient.getPatientId(), tenMinutesAgo, currentTime)
                .stream()
                .filter(r -> "OxygenSaturation".equals(r.getRecordType()))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp).reversed())
                .limit(2)
                .collect(Collectors.toList());

        if (boRecords.size() == 2 && (boRecords.get(0).getMeasurementValue() - boRecords.get(1).getMeasurementValue() >= 5)) {
            triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Rapid Blood Oxygen Drop Alert", currentTime));
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

        for (PatientRecord record : ecgRecords) {
            if (record.getMeasurementValue() < 50 || record.getMeasurementValue() > 100) {
                triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Abnormal Heart Rate Alert", currentTime));
            }
        }

        if (ecgRecords.size() > 1) {
            double previousValue = ecgRecords.get(0).getMeasurementValue();
            for (int i = 1; i < ecgRecords.size(); i++) {
                double currentValue = ecgRecords.get(i).getMeasurementValue();
                if (Math.abs(currentValue - previousValue) / previousValue > 0.10) {
                    triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Irregular Beat Alert", currentTime));
                    break;
                }
                previousValue = currentValue;
            }
        }
    }

    /**
     * Checks for an increasing trend in measurement values across records.
     *
     * @param records   The list of patient records.
     * @param threshold The minimum difference to consider a trend increasing.
     * @return true if an increasing trend is found, false otherwise.
     */
    private boolean checkIncreaseTrend(List<PatientRecord> records, int threshold) {
        for (int i = 0; i < records.size() - 1; i++) {
            if (records.get(i + 1).getMeasurementValue() - records.get(i).getMeasurementValue() <= threshold) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks for a decreasing trend in measurement values across records.
     *
     * @param records   The list of patient records.
     * @param threshold The minimum difference to consider a trend decreasing.
     * @return true if a decreasing trend is found, false otherwise.
     */
    private boolean checkDecreaseTrend(List<PatientRecord> records, int threshold) {
        for (int i = 0; i < records.size() - 1; i++) {
            if (records.get(i).getMeasurementValue() - records.get(i + 1).getMeasurementValue() <= threshold) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any record meets critical threshold criteria.
     *
     * @param records The list of patient records.
     * @param high    The high threshold to trigger an alert.
     * @param low     The low threshold to trigger an alert.
     * @return true if any record meets the threshold criteria, false otherwise.
     */
    private boolean checkThreshold(List<PatientRecord> records, double high, double low) {
        for (PatientRecord record : records) {
            if (record.getMeasurementValue() > high || record.getMeasurementValue() < low) {
                return true;
            }
        }
        return false;
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
