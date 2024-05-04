package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataReader;
import com.data_management.DataStorage;
import com.data_management.FileReader;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

class DataStorageANDFileReaderTest {

    @Test
    void testAddAndGetRecords() {

        DataReader reader = new FileReader("src/test/java/data_management/outputFilesTest");
        
        DataStorage storage = new DataStorage();
        
        try{
            reader.readData(storage);
        } catch(Exception e){
            System.out.println("smth went wrong");
        }

        List<PatientRecord> records = storage.getRecords(10, 1714748468033L, 1714748468034L);
        assertEquals(2, records.size()); // Check if two records are retrieved
        assertEquals(-0.34656395320945643, records.get(0).getMeasurementValue()); // Validate first record
        
        List<Patient> allRecords = storage.getAllPatients();
        assertEquals(1, allRecords.get(1).getPatientId()); // Check the first patient id 
        assertEquals(31, allRecords.get(7).getPatientId()); // Check the last patient id 
    }

    @Test
    void testGetRecordsNullBehaviour() {
        DataStorage storage = new DataStorage();
        List<PatientRecord> records = storage.getRecords(10, 1714748468033L, 1714748468034L);
        assertEquals(0, records.size()); // Check if the new empty Array was created
    }
}
