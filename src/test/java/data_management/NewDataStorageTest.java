package data_management;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Unit test for DataStorage class.
 */
public class NewDataStorageTest {
    private DataStorage storage;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        storage = new DataStorage();
    }

    /**
     * Tests the addPatientData method to ensure patient data is added and retrieved correctly.
     */
    @Test
    public void testAddPatientData() {
        storage.addPatientData(1, 75.5, "HeartRate", 1714748468033L);
        List<PatientRecord> records = storage.getRecords(1, 1714748468030L, 1714748468040L);
        assertEquals(1, records.size());
        assertEquals(75.5, records.get(0).getMeasurementValue(), 0.01);
    }

    /**
     * Tests the getRecords method when no records are present for the given parameters.
     */
    @Test
    public void testGetRecords_noRecords() {
        List<PatientRecord> records = storage.getRecords(1, 1714748468030L, 1714748468040L);
        assertTrue(records.isEmpty());
    }

    /**
     * Tests the getRecords method to ensure it retrieves records within the specified time range.
     */
    @Test
    public void testGetRecords_withinRange() {
        storage.addPatientData(1, 75.5, "HeartRate", 1714748468033L);
        storage.addPatientData(1, 80.0, "HeartRate", 1714748468035L);
        List<PatientRecord> records = storage.getRecords(1, 1714748468030L, 1714748468040L);
        assertEquals(2, records.size());
    }

    /**
     * Tests the getAllPatients method to ensure it retrieves all patients in the storage.
     */
    @Test
    public void testGetAllPatients() {
        storage.addPatientData(1, 75.5, "HeartRate", 1714748468033L);
        storage.addPatientData(2, 80.0, "BloodPressure", 1714748468035L);
        List<Patient> patients = storage.getAllPatients();
        assertEquals(2, patients.size());
    }
}
