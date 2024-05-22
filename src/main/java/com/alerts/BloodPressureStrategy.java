package com.alerts;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * This class implements the AlertStrategy interface and generates alerts
 * based on blood pressure readings of a patient. It extends the AlertGenerator
 * to utilize common alert generation functionality.
 */
public class BloodPressureStrategy extends AlertGenerator implements AlertStrategy {

    private DataStorage dataStorage;

    /**
     * Constructs a BloodPressureStrategy with the given data storage.
     *
     * @param dataStorage the data storage to retrieve patient records
     */
    public BloodPressureStrategy(DataStorage dataStorage) {
        super(dataStorage);
    }

    /**
     * Checks for blood pressure alerts for the given patient. It evaluates
     * both systolic and diastolic pressure records from the past 24 hours.
     *
     * @param patient the patient for whom alerts are to be checked
     */
    @Override
    public void checkAlert(Patient patient) {
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

    /**
     * Checks the pressure records against critical thresholds and trends, and
     * triggers appropriate alerts if conditions are met.
     *
     * @param records     the list of patient records to evaluate
     * @param type        the type of pressure (Systolic or Diastolic)
     * @param currentTime the current time in milliseconds
     * @param patient     the patient for whom alerts are to be generated
     */
    private void checkAndTriggerPressureAlerts(List<PatientRecord> records, String type, long currentTime, Patient patient) {
        // Check critical thresholds
        for (PatientRecord record : records) {
            if ((type.equals("Systolic") && (record.getMeasurementValue() > 180 || record.getMeasurementValue() < 90))) {
                super.triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Critical Pressure Threshold Alert (Systolic)", record.getTimestamp()));
            }
            if ((type.equals("Diastolic") && (record.getMeasurementValue() > 120 || record.getMeasurementValue() < 60))) {
                super.triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Critical Pressure Threshold Alert (Diastolic)", record.getTimestamp()));
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
                super.triggerAlert(new Alert(Integer.toString(patient.getPatientId()), type + " Pressure Increasing Trend Alert", currentTime));
            }
            if (decreasing) {
                super.triggerAlert(new Alert(Integer.toString(patient.getPatientId()), type + " Pressure Decreasing Trend Alert", currentTime));
            }
        }
    }
}
