package com.data_management;

import java.io.IOException;
import java.net.URI;

public interface DataReader {
    /**
     * Reads data from a specified source and stores it in the data storage.
     * 
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    void readDataFromFile(DataStorage dataStorage) throws IOException;

    /**
     * Connects to a WebSocket server and stores received data in the data storage.
     * 
     * @param serverUri the URI of the WebSocket server
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error connecting to the server or reading data
     */
    void readData(URI serverUri, DataStorage dataStorage) throws IOException;
}
