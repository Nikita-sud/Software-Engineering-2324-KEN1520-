package com.alerts;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * This class implements the AlertStrategy interface and generates alerts
 * based on oxygen saturation readings of a patient. It extends the AlertGenerator
 * to utilize common alert generation functionality.
 */
public class OxygenSaturationStrategy extends AlertGenerator implements AlertStrategy {
    private DataStorage dataStorage;

    /**
     * Constructs an OxygenSaturationStrategy with the given data storage.
     *
     * @param dataStorage the data storage to retrieve patient records
     */
    public OxygenSaturationStrategy(DataStorage dataStorage) {
        super(dataStorage);
    }

    /**
     * Checks for oxygen saturation alerts for the given patient. It evaluates
     * saturation records from the past 10 minutes.
     *
     * @param patient the patient for whom alerts are to be checked
     */
    @Override
    public void checkAlert(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = dataStorage.getRecords(patient.getPatientId(), currentTime - 600000, currentTime)
            .stream()
            .filter(r -> "Saturation".equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
            .collect(Collectors.toList());

        // Check for low saturation
        for (PatientRecord record : records) {
            if (record.getMeasurementValue() < 92) {
                super.triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Low Saturation Alert", record.getTimestamp()));
                break; // Ensure only the first applicable alert is triggered
            }
        }

        // Check for rapid drop in blood oxygen levels
        for (int i = 1; i < records.size(); i++) {
            double dropPercentage = 100.0 * (records.get(i - 1).getMeasurementValue() - records.get(i).getMeasurementValue()) / records.get(i - 1).getMeasurementValue();
            if (dropPercentage >= 5) {
                super.triggerAlert(new Alert(Integer.toString(patient.getPatientId()), "Rapid Blood Oxygen Drop Alert", records.get(i).getTimestamp()));
                break; // Ensure proper alert sequence
            }
        }
    }
}
