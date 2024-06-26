import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientReader;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    private DataStorage dataStorage;
    private AlertGenerator alertGenerator;
    private WebSocketClientReader webSocketClientReader;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private long currentTime;

    /**
     * Sets up the test environment before each test.
     * 
     * @throws URISyntaxException if the URI syntax is incorrect
     */
    @BeforeEach
    void setUp() throws URISyntaxException {
        dataStorage = DataStorage.getInstance();
        dataStorage.clear(); // Clear the data storage before each test
        alertGenerator = spy(new AlertGenerator(dataStorage));
        webSocketClientReader = spy(new WebSocketClientReader(new URI("ws://localhost:8080"), dataStorage));

        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        currentTime = System.currentTimeMillis();
    }

    /**
     * Restores the original output streams after each test.
     */
    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    /**
     * Tests the full integration of WebSocket client, data storage, and alert generation.
     * 
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testIntegration() throws Exception {
        doNothing().when(webSocketClientReader).connect();
        doNothing().when(webSocketClientReader).reconnect();

        webSocketClientReader.onOpen(mock(ServerHandshake.class));

        long timestamp = currentTime - 1000; // Ensure the timestamp falls within the expected range

        String message = "Patient ID: 1, Timestamp: " + timestamp + ", Label: ECG, Data: 160";
        webSocketClientReader.onMessage(message);

        List<Patient> patients = dataStorage.getAllPatients();
        assertFalse(patients.isEmpty(), "Patient data should not be empty");

        Patient patient = patients.get(0);
        assertEquals(1, patient.getPatientId(), "Patient ID should be 1");

        List<PatientRecord> records = dataStorage.getRecords(1, timestamp, timestamp);
        assertFalse(records.isEmpty(), "Patient records should not be empty");
        assertEquals(1, records.size(), "There should be exactly one record");
        assertEquals("ECG", records.get(0).getRecordType(), "The record type should be ECG");
        assertEquals(160, records.get(0).getMeasurementValue(), 0.001, "The measurement value should be 160");

        alertGenerator.evaluateData(patient);

        String output = outContent.toString();
        assertTrue(output.contains("Abnormal Heart Rate Alert"),
                "Expected 'Abnormal Heart Rate Alert' for very high heart rate.");
    }

    /**
     * Tests the integration with error handling during WebSocket client operations.
     * 
     * @throws Exception if any error occurs during the test
     */
    @Test
    public void testIntegrationWithErrorHandling() throws Exception {
        doThrow(new RuntimeException("Test exception")).when(webSocketClientReader).connect();
        doNothing().when(webSocketClientReader).reconnect();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        webSocketClientReader.onError(new Exception("Test error"));

        System.setErr(originalErr);

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Test error"));
        assertTrue(errOutput.contains("An error occurred"));
        verify(webSocketClientReader, atLeastOnce()).reconnect();
    }

    /**
     * Tests the behavior when the WebSocket client fails to connect during read data operation.
     * 
     * @param uri the URI to connect to
     * @throws URISyntaxException if the URI syntax is incorrect
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testReadDataConnectionAttemptFailure() throws URISyntaxException, IOException {
        WebSocketClientReader spyClient = spy(webSocketClientReader);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        doThrow(new RuntimeException("Connection attempt failed")).when(spyClient).connect();

        spyClient.readData(new URI("ws://localhost:8080"), dataStorage);

        System.setErr(originalErr);

        String errOutput = errContent.toString();
        assertTrue(errOutput.contains("Connection attempt failed: Connection attempt failed"));
    }

    /**
     * Tests the behavior when the WebSocket client successfully connects during read data operation.
     * 
     * @param uri the URI to connect to
     * @throws URISyntaxException if the URI syntax is incorrect
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testReadDataConnectionSuccess() throws URISyntaxException, IOException {
        WebSocketClientReader spyClient = spy(webSocketClientReader);

        doNothing().when(spyClient).connect();

        spyClient.readData(new URI("ws://localhost:8080"), dataStorage);

        verify(spyClient, times(1)).connect();
    }
}