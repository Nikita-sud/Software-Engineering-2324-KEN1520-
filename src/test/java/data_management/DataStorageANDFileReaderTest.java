package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataReaderFile;
import com.data_management.DataStorage;
import com.data_management.FileReader;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.io.IOException;
import java.util.List;

/**
 * Unit tests for DataStorage and DataReaderFile classes.
 * This class contains test cases to verify the functionality of DataStorage 
 * and reading data from files using DataReaderFile.
 */
class DataStorageANDFileReaderTest {

    /**
     * Tests adding patient data using DataReaderFile and retrieving the records from DataStorage.
     * It checks if the records are correctly stored and retrieved, and verifies patient IDs.
     */
    @Test
    void testAddAndGetRecords() {
        DataReaderFile reader = new FileReader("src/test/java/data_management/outputFilesTest");
        DataStorage storage = DataStorage.getInstance();
        
        try {
            reader.readData(storage);
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }

        List<PatientRecord> records = storage.getRecords(10, 1714748468033L, 1714748468034L);
        assertEquals(2, records.size()); // Check if two records are retrieved
        assertEquals(-0.34656395320945643, records.get(0).getMeasurementValue(), 0.01); // Validate first record
        
        List<Patient> allRecords = storage.getAllPatients();
        assertEquals(1, allRecords.get(0).getPatientId()); // Check the first patient id 
        assertEquals(31, allRecords.get(7).getPatientId()); // Check the last patient id 
    }

    /**
     * Tests the behavior of the getRecords method when no records are present.
     * It verifies that an empty list is returned.
     */
    @Test
    void testGetRecordsNullBehaviour() {
        DataStorage storage = DataStorage.getInstance();
        List<PatientRecord> records = storage.getRecords(10, 1714748468033L, 1714748468034L);
        assertTrue(records.isEmpty()); // Check if the new empty Array was created
    }

    /**
     * Tests the readData method of DataReaderFile when an invalid directory path is provided.
     * It verifies that an IOException is thrown and the error message is correct.
     *
     * @throws IOException if there is an error reading the data from the file
     */
    @Test
    void testReadDataFileReadingError() {
        DataReaderFile reader = new FileReader("invalid/directory/path");
        DataStorage storage = DataStorage.getInstance();

        Exception exception = assertThrows(IOException.class, () -> {
            reader.readData(storage);
        });

        String expectedMessage = "Error walking through directory";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}