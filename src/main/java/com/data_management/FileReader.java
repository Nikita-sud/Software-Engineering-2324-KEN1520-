package com.data_management;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileReader implements DataReader{
    // variable stores path to the file
    private String filePath;

    public FileReader(String filepath){
        this.filePath = filePath;
    }
    
    /**
     * reads data from a specified source and stores it in the data storage.
     * 
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    public void readData(DataStorage dataStorage) throws IOException{
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))){
            String line;
            while ((line =reader.readLine())!=null) {
                if(filePath.endsWith(".csv")){
                    // Split the line on the words and assign each word to the specific variable
                    String[] arrLineWords= line.split(",");
                    int patientId = Integer.parseInt(arrLineWords[0]);
                    double measurementValue = Double.parseDouble(arrLineWords[1]);
                    String recordType = arrLineWords[2];
                    long timestamp = Long.parseLong(arrLineWords[3]);
                    // Add new data to the dataStorage
                    dataStorage.addPatientData(patientId,measurementValue,recordType, timestamp);
                }
            }

        } catch (Exception IOException){
            throw new IOException("Error in reading data");
        }
    }
}
