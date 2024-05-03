package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;

class AlertGeneratorTest {

    private AlertGenerator alertGenerator;
    private DataStorage mockDataStorage;
    private final long currentTime = System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        mockDataStorage = Mockito.mock(DataStorage.class);
        alertGenerator = new AlertGenerator(mockDataStorage);
    }

    @Test
    void testEvaluateBloodPressureCriticalAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> bpRecords = Arrays.asList(
            new PatientRecord(1, 200, "SystolicPressure", currentTime),
            new PatientRecord(1, 120, "DiastolicPressure", currentTime - 1000)
        );
        
        Mockito.when(mockDataStorage.getRecords(1, currentTime - 86400000, currentTime))
               .thenReturn(bpRecords);

        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        alertGenerator.evaluateData(patient);

        assertTrue(out.toString().contains("Critical Systolic Pressure Alert"));
        assertTrue(out.toString().contains("Critical Diastolic Pressure Alert"));
        System.setOut(System.out);
    }

    @Test
    void testEvaluateBloodOxygenRapidDropAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> boRecords = Arrays.asList(
            new PatientRecord(1, 95, "Saturation", currentTime - 1000),
            new PatientRecord(1, 85, "Saturation", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(1, currentTime - 600000, currentTime))
               .thenReturn(boRecords);

        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        alertGenerator.evaluateData(patient);

        assertTrue(out.toString().contains("Rapid Blood Oxygen Drop Alert"));
        System.setOut(System.out);
    }

    @Test
    void testEvaluateECGDataAbnormalHeartRateAlert() {
        Patient patient = new Patient(1);
        List<PatientRecord> ecgRecords = Arrays.asList(
            new PatientRecord(1, 55, "ECG", currentTime - 2000),
            new PatientRecord(1, 130, "ECG", currentTime - 1000),
            new PatientRecord(1, 50, "ECG", currentTime)
        );

        Mockito.when(mockDataStorage.getRecords(1, currentTime - 3600000, currentTime))
               .thenReturn(ecgRecords);

        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        alertGenerator.evaluateData(patient);

        assertTrue(out.toString().contains("Abnormal Heart Rate Alert"));
        System.setOut(System.out);
    }
}
