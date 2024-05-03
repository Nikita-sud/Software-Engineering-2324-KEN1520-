package alerts_management;


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
    void testEvaluateBloodPressureCriticalAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> bpRecords = Arrays.asList(
            new PatientRecord(1, 200, "SystolicPressure", currentTime),
            new PatientRecord(1, 120, "DiastolicPressure", currentTime - 1000)
        );

        Mockito.when(mockDataStorage.getRecords(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong()))
               .thenReturn(bpRecords);

        alertGenerator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Critical Systolic Pressure Alert") ||
                   outContent.toString().contains("Critical Diastolic Pressure Alert"));
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
}
