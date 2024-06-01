package data_management;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Unit test for the {@link DataStorage} class.
 * This class contains test cases to verify the functionality of the DataStorage methods.
 */
public class NewDataStorageTest {
    private DataStorage storage;

    /**
     * Sets up the test environment before each test case.
     * Initializes the DataStorage instance and clears any existing data.
     */
    @Before
    public void setUp() {
        storage = DataStorage.getInstance();
        storage.clear(); 
    }

    /**
     * Cleans up the test environment after each test case.
     * Clears any data in the DataStorage instance.
     */
    @After
    public void tearDown() {
        storage.clear(); 
    }

    /**
     * Tests the addPatientData method by adding a patient record and 
     * verifying if it is correctly stored and retrievable.
     */
    @Test
    public void testAddPatientData() {
        storage.addPatientData(1, 75.5, "HeartRate", 1714748468033L);
        List<PatientRecord> records = storage.getRecords(1, 1714748468030L, 1714748468040L);
        System.out.println("Records: " + records);
        assertEquals(1, records.size());
        assertEquals(75.5, records.get(0).getMeasurementValue(), 0.01);
    }

    /**
     * Tests the getAllPatients method by adding multiple patient records and 
     * verifying if all patient data can be retrieved.
     */
    @Test
    public void testGetAllPatients() {
        storage.addPatientData(1, 75.5, "HeartRate", 1714748468033L);
        storage.addPatientData(2, 80.0, "BloodPressure", 1714748468035L);
        List<Patient> patients = storage.getAllPatients();
        System.out.println("Patients: " + patients);
        assertEquals(2, patients.size());
    }

    /**
     * Tests the getRecords method by adding multiple patient records and 
     * verifying if the records within a specific time range can be retrieved.
     */
    @Test
    public void testGetRecords_withinRange() {
        storage.addPatientData(1, 75.5, "HeartRate", 1714748468033L);
        storage.addPatientData(1, 80.0, "HeartRate", 1714748468034L);
        storage.addPatientData(1, 82.0, "HeartRate", 1714748468035L);
        List<PatientRecord> records = storage.getRecords(1, 1714748468030L, 1714748468040L);
        System.out.println("Records within range: " + records);
        assertEquals(3, records.size());
    }

    /**
     * Tests the getRecords method when there are no records for the given patient 
     * within the specified time range. 
     * Verifies if the method correctly returns an empty list.
     */
    @Test
    public void testGetRecords_noRecords() {
        List<PatientRecord> records = storage.getRecords(1, 1714748468030L, 1714748468040L);
        assertTrue(records.isEmpty());
    }
}