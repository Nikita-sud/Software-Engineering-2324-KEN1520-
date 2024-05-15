package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

/**
 * A WebSocket client that reads data from a server and stores it in a DataStorage instance.
 */
public class WebSocketClientReader extends WebSocketClient implements DataReader {
    private DataStorage dataStorage;

    /**
     * Constructs a new WebSocketClientReader.
     *
     * @param serverUri   the URI of the WebSocket server
     * @param dataStorage the DataStorage instance to store the received data
     */
    public WebSocketClientReader(URI serverUri, DataStorage dataStorage) {
   
        super(serverUri); // Initialisation of parent class WebSocketClient with server URI
        this.dataStorage = dataStorage; // Save a reference to the DataStorage instance for use in onMessage
    }

    /**
     * Called when the WebSocket connection is opened.
     *
     * @param handshakedata the handshake data
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
    }

    /**
     * Called when a message is received from the server.
     *
     * @param message the received message
     */
    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        try {

            // Checking the message format
            String[] parts = message.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid message format");
            }

            // Message parsing

            // int patientId = Integer.parseInt(parts[0]);
            // long timestamp = Long.parseLong(parts[1]);
            // String label = parts[2];
            // double data = Double.parseDouble(parts[3]);
            int patientId = Integer.parseInt(parts[0].split(": ")[1]);
            long timestamp = Long.parseLong(parts[1].split(": ")[1]);
            String label = parts[2].split(": ")[1];

            String dataStr = parts[3].split(": ")[1];

            // Check if the string contains a percent character
            if (dataStr.contains("%")) {
                dataStr = dataStr.replace("%", "");
            }
            
            double data = Double.parseDouble(dataStr);                            

            // Adding data to dataStorage
            dataStorage.addPatientData(patientId, data, label, timestamp);

        } catch (NumberFormatException e) {
            // Parsing error handling of numeric data (e.g. patientId or timestamp)
            System.err.println("Error parsing message: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // Handling an invalid message format error (e.g. not enough parts in the message)
            System.err.println("Received invalid message: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Handle any other unexpected errors
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called when the WebSocket connection is closed.
     *
     * @param code   the status code
     * @param reason the reason for the closure
     * @param remote whether the closure was initiated by the remote peer
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with exit code " + code + " additional info: " + reason);

        // Try to reconnect if the connection is closed unexpectedly
        if (remote) {
            System.out.println("Attempting to reconnect...");
            try {
                this.reconnect();
            } catch (Exception e) {
                System.err.println("Reconnection attempt failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Called when an error occurs.
     *
     * @param ex the exception that occurred
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred: " + ex.getMessage());
        ex.printStackTrace();

        // Try to reconnect in case of connection error
        try {
            this.reconnect();
        } catch (Exception e) {
            System.err.println("Reconnection attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Connects to the WebSocket server and starts reading data.
     *
     * @param serverUri   the URI of the WebSocket server
     * @param dataStorage the DataStorage instance to store the received data
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void readData(URI serverUri, DataStorage dataStorage) throws IOException {
        this.dataStorage = dataStorage;

        // Try to reconnect if the connection is closed unexpectedly
        try {
            this.connect();
        } catch (Exception e) {
            System.err.println("Connection attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
