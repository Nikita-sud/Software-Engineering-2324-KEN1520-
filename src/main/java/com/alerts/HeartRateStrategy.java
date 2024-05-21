package com.alerts;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.crypto.Data;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class HeartRateStrategy extends AlertGenerator implements AlertStrategy{

    private DataStorage dataStorage;

    public HeartRateStrategy(DataStorage dataStorage) {
        super(dataStorage);
    }

    public void checkAlert(Patient patient) {
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

   
}
