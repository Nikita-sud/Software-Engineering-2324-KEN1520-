package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataReader;
import com.data_management.DataStorage;
import com.data_management.FileReader;
import com.data_management.PatientRecord;

import java.util.List;

class DataStorageTest {

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
    }
}
