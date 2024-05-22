package com.alerts;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * This class implements the AlertStrategy interface and generates alerts
 * based on heart rate readings from ECG records of a patient. It extends
 * the AlertGenerator to utilize common alert generation functionality.
 */
public class HeartRateStrategy extends AlertGenerator implements AlertStrategy {

    private DataStorage dataStorage;

    /**
     * Constructs a HeartRateStrategy with the given data storage.
     *
     * @param dataStorage the data storage to retrieve patient records
     */
    public HeartRateStrategy(DataStorage dataStorage) {
        super(dataStorage);
    }

    /**
     * Checks for heart rate alerts for the given patient. It evaluates
     * ECG records from the past hour.
     *
     * @param patient the patient for whom alerts are to be checked
     */
    @Override
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

    /**
     * Calculates the average interval between successive ECG records.
     *
     * @param ecgRecords the list of ECG records to evaluate
     * @return the average interval between records in milliseconds
     */
    private double calculateAverageInterval(List<PatientRecord> ecgRecords) {
        long totalInterval = 0;
        for (int i = 1; i < ecgRecords.size(); i++) {
            totalInterval += (ecgRecords.get(i).getTimestamp() - ecgRecords.get(i - 1).getTimestamp());
        }
        return totalInterval / (double) (ecgRecords.size() - 1);
    }
}
