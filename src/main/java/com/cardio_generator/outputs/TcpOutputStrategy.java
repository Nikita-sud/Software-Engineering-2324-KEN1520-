package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Strategy for outputting data via TCP.
 * This class sets up a server that can send patient data to a connected TCP client.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private ExecutorService clientAcceptorExecutor;

    /**
     * Constructs a TcpOutputStrategy that starts a server on the specified port.
     *
     * @param port the port number on which the server should listen for clients
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Executors should be managed and shutdown appropriately.
            clientAcceptorExecutor = Executors.newSingleThreadExecutor();
            clientAcceptorExecutor.submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            System.err.println("Error starting TCP server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends the output data to the connected TCP client.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time at which the data was recorded
     * @param label     the label describing the type of data
     * @param data      the actual data to send
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        // Ensure there is a connection before attempting to write out.
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        } else {
            System.err.println("Error: No TCP client connected, cannot send data");
        }
    }

    /**
     * Closes the server and client sockets along with the output stream.
     */
    public void close() {
        try {
            if (out != null) {
                out.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (clientAcceptorExecutor != null && !clientAcceptorExecutor.isShutdown()) {
                clientAcceptorExecutor.shutdown();
            }
        } catch (IOException e) {
            System.err.println("Error closing TCP resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
