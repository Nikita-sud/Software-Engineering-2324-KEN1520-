package com.alerts;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.alerts.AlertStrategy;
import com.alerts.AlertGenerator;

public class BloodPressureStrategy extends AlertGenerator implements AlertStrategy{

    private DataStorage dataStorage;

    public BloodPressureStrategy(DataStorage dataStorage) {
        super(dataStorage);
    }

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
                super.triggerAlert(new Alert(Integer.toString(patient.getPatientId()), type + "Pressure Increasing Trend Alert", currentTime));
            }
            if (decreasing) {
                super.triggerAlert(new Alert(Integer.toString(patient.getPatientId()), type + "Pressure Decreasing Trend Alert", currentTime));
            }
        }
    }



    
}
