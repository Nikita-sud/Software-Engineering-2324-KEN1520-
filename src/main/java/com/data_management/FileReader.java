package com.data_management;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class FileReader implements DataReader{
    
    /**
     * reads data from a specified source and stores it in the data storage.
     * 
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    public void readData(DataStorage dataStorage) throws IOException {
        String directoryPath = "src\\main\\java\\com\\cardio_generator\\outputs";

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .forEach(path -> {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] arrLineWords = line.split(",");
                            int patientId = Integer.parseInt(arrLineWords[0]);
                            double measurementValue = Double.parseDouble(arrLineWords[1]);
                            String recordType = arrLineWords[2];
                            long timestamp = Long.parseLong(arrLineWords[3]);
                            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + path);
                    }
                });
        } catch (IOException e) {
            throw new IOException("Error walking through directory: " + directoryPath, e);
        }
    }
}
