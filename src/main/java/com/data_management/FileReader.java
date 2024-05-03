package com.data_management;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileReader implements DataReader{
    private String directoryPath;

    /**
     * constructor for the FileReader
     * 
     * @param directoryPath the path to the directory wit the output files (eg "src\\test\\java\\data_management\\outputFilesTest")
     */
    public FileReader(String directoryPath){
        this.directoryPath = directoryPath;
    }
    
    /**
     * reads data from a specified source and stores it in the data storage.
     * 
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    public void readData(DataStorage dataStorage) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            // Find all the txt files and reach one by one
            paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .forEach(path -> {
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Parse the file 
                            System.out.println(line);

                            // if the structure of the file is patientId, measurementValue, recordType, timestamp 
                            // String[] arrLineWords = line.split(",");
                            // int patientId = Integer.parseInt(arrLineWords[0]);
                            // double measurementValue = Double.parseDouble(arrLineWords[1]);
                            // String recordType = arrLineWords[2];
                            // long timestamp = Long.parseLong(arrLineWords[3]);
                            // dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);

                            // if the structure of the file is "Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n"
                            String[] parts = line.split(", ");
                            int patientId = Integer.parseInt(parts[0].split(": ")[1]);
                            long timestamp = Long.parseLong(parts[1].split(": ")[1]);
                            String label = parts[2].split(": ")[1];

                            String dataStr = parts[3].split(": ")[1];
                            // Check if the string contains a percent character
                            if (dataStr.contains("%")) {
                                dataStr = dataStr.replace("%", "");
                            }
                            
                            double data = Double.parseDouble(dataStr);                            
                            dataStorage.addPatientData(patientId, data, label, timestamp);
                        }

                    } catch (IOException e) {
                        System.out.println("Error reading file: " + path);
                    }
                });
        } catch (IOException e) {
            throw new IOException("Error walking through directory: " + directoryPath, e);
        }
    }

}
