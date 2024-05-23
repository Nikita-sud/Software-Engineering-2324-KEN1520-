package alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.alerts.AlertGenerator;
import com.alerts.BloodPressureStrategy;
import com.alerts.HeartRateStrategy;
import com.alerts.OxygenSaturationStrategy;
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

    /**
     * Tests critical alert for high systolic blood pressure.
     */
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

    /**
     * Tests critical alert for low diastolic blood pressure.
     */
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

    /**
     * Tests no critical alerts for normal blood pressure values.
     */
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

    /**
     * Tests hypotensive hypoxemia alert.
     */
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

    /**
     * Tests rapid blood oxygen drop alert.
     */
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

        assertTrue(outContent.toString().contains("Rapid Blood Oxygen Drop Alert"),
                "Expected 'Rapid Blood Oxygen Drop Alert' but got: " + outContent.toString());
    }

    /**
     * Tests low oxygen saturation alert for borderline saturation levels.
     */
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

    /**
     * Tests no alerts for normal heart rate values.
     */
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

    /**
     * Tests abnormal heart rate alert for very high heart rate.
     */
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

    /**
     * Tests exception when null patient is provided.
     */
    @Test
    void testEvaluateDataWithNullPatient() {
        DataStorage mockDataStorage = Mockito.mock(DataStorage.class);
        AlertGenerator alertGenerator = new AlertGenerator(mockDataStorage);

        Exception exception = assertThrows(NullPointerException.class, () -> {
            alertGenerator.evaluateData(null);
        });

        assertEquals("Patient data is null.", exception.getMessage());
    }

    /**
     * Tests no alerts for boundary condition blood pressure values.
     */
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

    /**
     * Tests abnormal heart rate alert for varying heart rate values.
     */
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

    /**
     * Tests high heart rate alert for very high ECG values.
     */
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

    /**
     * Tests low heart rate alert for very low ECG values.
     */
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

    /**
     * Tests no alerts for normal values of multiple record types.
     */
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

    /**
     * Tests decreasing blood pressure trend alert.
     */
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

    /**
     * Tests increasing blood pressure trend alert.
     */
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

    /**
     * Tests irregular heartbeat alert.
     */
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

    /**
     * Tests increasing trend alert for blood pressure strategy.
     */
    @Test
    void testBloodPressureIncreasingStrategy(){
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = Arrays.asList(
            new PatientRecord(1, 100, "SystolicPressure", currentTime-10),
            new PatientRecord(1, 111, "SystolicPressure", currentTime-5),
            new PatientRecord(1, 122, "SystolicPressure", currentTime)
        );
        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(records);
        BloodPressureStrategy strategy = new BloodPressureStrategy(mockDataStorage);
        alertGenerator.setStrategy(strategy);

        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Pressure Increasing Trend Alert"));
    }

    /**
     * Tests decreasing trend alert for blood pressure strategy.
     */
    @Test
    void testBloodPressureDecreasingStrategy(){
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> records = Arrays.asList(
            new PatientRecord(1, 120, "SystolicPressure", currentTime-10),
            new PatientRecord(1, 109, "SystolicPressure", currentTime-5),
            new PatientRecord(1, 89, "SystolicPressure", currentTime)
        );
        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(records);
        BloodPressureStrategy strategy = new BloodPressureStrategy(mockDataStorage);
        alertGenerator.setStrategy(strategy);

        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Pressure Decreasing Trend Alert"));
    }

    /**
     * Tests irregular beat alert for heart rate strategy.
     */
    @Test
    void testHeartRateStrategy(){
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 60, "ECG", currentTime - 3000),
            new PatientRecord(1, 60, "ECG", currentTime - 2000), 
            new PatientRecord(1, 60, "ECG", currentTime - 500) 
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(ecgRecords);
        HeartRateStrategy strategy = new HeartRateStrategy(mockDataStorage);
        alertGenerator.setStrategy(strategy);

        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Irregular Beat Alert"),
            "Expected 'Irregular Beat Alert' but got: " + outContent.toString());
    }

    /**
     * Tests low saturation alert for oxygen saturation strategy.
     */
    @Test
    void testOxygenSaturationStrategy(){
        Patient patient = new Patient(1);
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> boRecords = Arrays.asList(
            new PatientRecord(1, 92, "Saturation", currentTime - 30000), // exactly at lower normal limit
            new PatientRecord(1, 91, "Saturation", currentTime)          // just below the limit
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(boRecords);
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy(mockDataStorage);
        alertGenerator.setStrategy(strategy);

        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Low Saturation Alert"),
                "Expected 'Low Oxygen Saturation Alert' for borderline saturation levels.");
    }

    /**
     * Tests exception when null strategy is provided.
     */
    @Test
    void testEvaluateDataWithNullStrategy() {
        Patient patient = new Patient(1);
        Exception exception = assertThrows(NullPointerException.class, () -> {
            alertGenerator.setStrategy(null);
            alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        });
        assertEquals("Strategy must not be null.", exception.getMessage());
    }

    /**
     * Tests no alerts for empty records.
     */
    @Test
    void testEvaluateDataWithEmptyRecords() {
        Patient patient = new Patient(1);
        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(Arrays.asList());
        BloodPressureStrategy strategy = new BloodPressureStrategy(mockDataStorage);
        alertGenerator.setStrategy(strategy);
        alertGenerator.evaluateData(patient);
        assertTrue(outContent.toString().isEmpty(), "Expected no alerts for empty records, but got: " + outContent.toString());
    }

    /**
     * Tests abnormal heart rate alert for low, normal, and high heart rate values.
     */
    @Test
    void testEvaluateHeartRateLowNormalHigh() {
        Patient patient = new Patient(1);
        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 45, "ECG", currentTime - 3000),
            new PatientRecord(1, 75, "ECG", currentTime - 2000),
            new PatientRecord(1, 110, "ECG", currentTime - 1000)
        );
        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(ecgRecords);
        HeartRateStrategy strategy = new HeartRateStrategy(mockDataStorage);
        alertGenerator.setStrategy(strategy);
        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Abnormal Heart Rate Alert"));
    }

    /**
     * Tests rapid blood oxygen drop alert for high drop in saturation.
     */
    @Test
    void testEvaluateOxygenSaturationHighDropAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> boRecords = Arrays.asList(
            new PatientRecord(1, 98, "Saturation", currentTime - 60000),
            new PatientRecord(1, 70, "Saturation", currentTime)
        );
        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(boRecords);
        OxygenSaturationStrategy strategy = new OxygenSaturationStrategy(mockDataStorage);
        alertGenerator.setStrategy(strategy);
        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Rapid Blood Oxygen Drop Alert"));
    }

    /**
     * Tests mixed strategies for different record types.
     */
    @Test
    void testEvaluateMixedStrategies() {
        Patient patient = new Patient(1);
        List<PatientRecord> mixedRecords = Arrays.asList(
            new PatientRecord(1, 160, "ECG", currentTime),
            new PatientRecord(1, 90, "Saturation", currentTime)
        );
        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(mixedRecords);

        HeartRateStrategy heartRateStrategy = new HeartRateStrategy(mockDataStorage);
        alertGenerator.setStrategy(heartRateStrategy);
        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Abnormal Heart Rate Alert"));

        OxygenSaturationStrategy oxygenSaturationStrategy = new OxygenSaturationStrategy(mockDataStorage);
        alertGenerator.setStrategy(oxygenSaturationStrategy);
        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Low Saturation Alert"));
    }

    /**
     * Tests no alerts for normal blood pressure values.
     */
    @Test
    void testEvaluateBloodPressureNoAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> bpRecords = Arrays.asList(
            new PatientRecord(1, 120, "SystolicPressure", currentTime),
            new PatientRecord(1, 80, "DiastolicPressure", currentTime)
        );
        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(bpRecords);
        BloodPressureStrategy strategy = new BloodPressureStrategy(mockDataStorage);
        alertGenerator.setStrategy(strategy);
        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertFalse(outContent.toString().contains("Alert"));
    }

    /**
     * Tests multiple conditions for different strategies.
     */
    @Test
    void testEvaluateMultipleConditions() {
        Patient patient = new Patient(1);
        List<PatientRecord> records = Arrays.asList(
            new PatientRecord(1, 200, "SystolicPressure", currentTime),
            new PatientRecord(1, 85, "Saturation", currentTime - 60000)
        );
        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(records);
        BloodPressureStrategy bpStrategy = new BloodPressureStrategy(mockDataStorage);
        alertGenerator.setStrategy(bpStrategy);
        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Critical Pressure Threshold Alert (Systolic)"));

        OxygenSaturationStrategy oxygenStrategy = new OxygenSaturationStrategy(mockDataStorage);
        alertGenerator.setStrategy(oxygenStrategy);
        alertGenerator.evaluateDataSTRATEGY_PATTERN(patient);
        assertTrue(outContent.toString().contains("Low Saturation Alert"));
    }

}
