package alerts;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AlertGeneratorTest {

    private AlertGenerator alertGenerator;
    private DataStorage mockDataStorage;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private long currentTime;

    @BeforeEach
    void setUp() {
        mockDataStorage = Mockito.mock(DataStorage.class);
        alertGenerator = new AlertGenerator(mockDataStorage);
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        currentTime = System.currentTimeMillis();
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void testEvaluateSystolicPressureCriticalAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> systolicRecord = Arrays.asList(
            new PatientRecord(1, 200, "SystolicPressure", currentTime)  
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(systolicRecord);

        alertGenerator.evaluateData(patient);

        String output = outContent.toString();
        assertTrue(output.contains("Critical Pressure Threshold Alert (Systolic)"), 
            "Expected 'Critical Pressure Threshold Alert' for high systolic pressure but got: " + output);
    }

    @Test
    void testEvaluateDiastolicPressureCriticalAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> diastolicRecord = Arrays.asList(
            new PatientRecord(1, 10, "DiastolicPressure", currentTime) 
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(diastolicRecord);

        alertGenerator.evaluateData(patient);

        String output = outContent.toString();
        assertTrue(output.contains("Critical Pressure Threshold Alert (Diastolic)"), 
            "Expected 'Critical Pressure Threshold Alert' for low diastolic pressure but got: " + output);
    }



    @Test
    void testEvaluateBloodPressureNormal() {
        Patient patient = new Patient(1);
        List<PatientRecord> bpRecords = Arrays.asList(
            new PatientRecord(1, 120, "SystolicPressure", currentTime),
            new PatientRecord(1, 80, "DiastolicPressure", currentTime - 1000)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(bpRecords);

        alertGenerator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Critical Systolic Pressure Alert") ||
                    outContent.toString().contains("Critical Diastolic Pressure Alert"),
                    "No critical alert should be triggered for normal BP values.");
    }

    @Test
    void testEvaluateHypotensiveHypoxemia() {
        int patientId = 1;
        List<PatientRecord> bpAndSaturationRecords = Arrays.asList(
            new PatientRecord(patientId, 89, "SystolicPressure", currentTime),
            new PatientRecord(patientId, 91, "Saturation", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.eq(patientId), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(bpAndSaturationRecords);

        alertGenerator.evaluateData(new Patient(patientId));

        assertTrue(outContent.toString().contains("Hypotensive Hypoxemia Alert"));
    }



    @Test
    void testEvaluateBloodOxygenRapidDropAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> boRecords = Arrays.asList(
            new PatientRecord(1, 95, "Saturation", currentTime - 60000),  // one minute ago
            new PatientRecord(1, 85, "Saturation", currentTime)          // now
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(boRecords);

        alertGenerator.evaluateData(patient);

        // Assert that the specific alert was triggered
        assertTrue(outContent.toString().contains("Rapid Blood Oxygen Drop Alert"),
                "Expected 'Rapid Blood Oxygen Drop Alert' but got: " + outContent.toString());
    }

    @Test
    void testEvaluateBloodOxygenBorderline() {
        Patient patient = new Patient(1);
        List<PatientRecord> boRecords = Arrays.asList(
            new PatientRecord(1, 92, "Saturation", currentTime - 30000), // exactly at lower normal limit
            new PatientRecord(1, 91, "Saturation", currentTime)          // just below the limit
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(boRecords);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Low Saturation Alert"),
                "Expected 'Low Oxygen Saturation Alert' for borderline saturation levels.");
    }


    @Test
    void testEvaluateECGDataNormalHeartRate() {
        Patient patient = new Patient(1);
        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 70, "ECG", currentTime - 2000), // Normal heart rate values
            new PatientRecord(1, 75, "ECG", currentTime - 1000),
            new PatientRecord(1, 72, "ECG", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(ecgRecords);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().isEmpty(), "Expected no alerts, but got: " + outContent.toString());
    }

    @Test
    void testEvaluateECGDataVeryHighHeartRateAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 160, "ECG", currentTime - 1000) // very high heart rate
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(ecgRecords);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Abnormal Heart Rate Alert"),
                "Expected 'Abnormal Heart Rate Alert' for very high ECG rate.");
    }


    @Test
    void testEvaluateDataWithNullPatient() {
        DataStorage mockDataStorage = Mockito.mock(DataStorage.class);
        AlertGenerator alertGenerator = new AlertGenerator(mockDataStorage);

        Exception exception = assertThrows(NullPointerException.class, () -> {
            alertGenerator.evaluateData(null);
        });

        assertEquals("Patient data is null.", exception.getMessage());
    }

    @Test
    void testCheckAndTriggerPressureAlertsBoundaryCondition() {
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> bpRecords = Arrays.asList(
            new PatientRecord(1, 179, "SystolicPressure", currentTime),
            new PatientRecord(1, 119, "DiastolicPressure", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(bpRecords);

        alertGenerator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Critical Systolic Pressure Alert"),
                    "Boundary condition should not trigger an alert.");
    }



    @Test
    void testEvaluateECGDataAbnormalHeartRateAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 55, "ECG", currentTime - 2000),
            new PatientRecord(1, 130, "ECG", currentTime - 1000),
            new PatientRecord(1, 50, "ECG", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(ecgRecords);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Abnormal Heart Rate Alert"));
    }

    @Test
    void testEvaluateECGDataHighHeartRateAlert() {
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 160, "ECG", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(ecgRecords);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Abnormal Heart Rate Alert"),
                "Expected 'Abnormal Heart Rate Alert' for very high heart rate.");
    }

    @Test
    void testEvaluateECGDataLowHeartRateAlert() {
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 45, "ECG", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(ecgRecords);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Abnormal Heart Rate Alert"),
                "Expected 'Abnormal Heart Rate Alert' for very low heart rate.");
    }

    @Test
    void testEvaluateDataWithMultipleRecordTypes() {
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> mixedRecords = Arrays.asList(
            new PatientRecord(1, 120, "BloodPressure", currentTime),
            new PatientRecord(1, 75, "ECG", currentTime),
            new PatientRecord(1, 98, "Saturation", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(mixedRecords);

        alertGenerator.evaluateData(patient);

        assertFalse(outContent.toString().contains("Alert"),
                    "No alerts should be triggered for normal values.");
    }

    @Test
    void testEvaluateBloodPressureDecreasingTrends() {
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = Arrays.asList(
            new PatientRecord(1, 120, "SystolicPressure", currentTime-10),
            new PatientRecord(1, 109, "SystolicPressure", currentTime-5),
            new PatientRecord(1, 89, "SystolicPressure", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(records);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Pressure Decreasing Trend Alert"));
    }

    @Test
    void testEvaluateBloodPressureIncreasingTrends() {
        Patient patient = new Patient(1);
        List<PatientRecord> records = Arrays.asList(
            new PatientRecord(1, 100, "SystolicPressure", currentTime-10),
            new PatientRecord(1, 111, "SystolicPressure", currentTime-5),
            new PatientRecord(1, 122, "SystolicPressure", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(records);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Pressure Increasing Trend Alert"));
    }

    @Test
    void testEvaluateECGIrregularHeartbeatAlert() {
        Patient patient = new Patient(1);
        long baseTime = System.currentTimeMillis();

        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 60, "ECG", baseTime - 3000),
            new PatientRecord(1, 60, "ECG", baseTime - 2000), 
            new PatientRecord(1, 60, "ECG", baseTime - 500) 
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(ecgRecords);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Irregular Beat Alert"),
            "Expected 'Irregular Beat Alert' but got: " + outContent.toString());
    }



}
