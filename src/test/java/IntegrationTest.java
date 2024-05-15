import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientReader;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import java.io.IOException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IntegrationTest {
    private DataStorage dataStorage;
    private AlertGenerator alertGenerator;
    private WebSocketClientReader webSocketClientReader;

    @Before
    public void setUp() throws URISyntaxException {
        dataStorage = new DataStorage();
        alertGenerator = spy(new AlertGenerator(dataStorage)); // Spying on AlertGenerator
        webSocketClientReader = spy(new WebSocketClientReader(new URI("ws://localhost:8080"), dataStorage));
    }

    @Test
    public void testIntegration() throws Exception {
        doNothing().when(webSocketClientReader).connect();
        doNothing().when(webSocketClientReader).reconnect();

        webSocketClientReader.onOpen(mock(ServerHandshake.class));

        String message = "Patient ID: 1, Timestamp: 1714748468033, Label: ECG, Data: 120";
        webSocketClientReader.onMessage(message);

        List<Patient> patients = dataStorage.getAllPatients();
        assertFalse("Patient data should not be empty", patients.isEmpty());

        Patient patient = patients.get(0);
        assertEquals("Patient ID should be 1", 1, patient.getPatientId());

        // Verify the data was stored correctly
        List<PatientRecord> records = dataStorage.getRecords(1, 1714748468033L, 1714748468033L);
        assertFalse("Patient records should not be empty", records.isEmpty());
        assertEquals("There should be exactly one record", 1, records.size());
        assertEquals("The record type should be ECG", "ECG", records.get(0).getRecordType());
        assertEquals("The measurement value should be 120", 120, records.get(0).getMeasurementValue(), 0.001);

        // Capture the alerts triggered
        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        doNothing().when(alertGenerator).triggerAlert(alertCaptor.capture());

        alertGenerator.evaluateData(patient);

        // List<Alert> alerts = alertCaptor.getAllValues();
        // assertFalse("Alerts should not be empty", alerts.isEmpty());
        // assertTrue("There should be an alert for abnormal heart rate", alerts.stream().anyMatch(alert -> alert.getCondition().contains("Abnormal Heart Rate Alert")));
    }

    @Test
    public void testIntegrationWithErrorHandling() throws Exception {
        doThrow(new RuntimeException("Test exception")).when(webSocketClientReader).connect();
        doNothing().when(webSocketClientReader).reconnect();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        webSocketClientReader.onError(new Exception("Test error"));

        System.setErr(originalErr);

        // String errOutput = errContent.toString();
        // assertTrue("Error output should contain 'Test error'", errOutput.contains("An error occurred: Test error"));
        // assertTrue("Error output should contain 'Reconnection attempt failed'", errOutput.contains("Reconnection attempt failed: Test exception"));
        // verify(webSocketClientReader, atLeastOnce()).reconnect();
    }

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
        assertTrue("Error output should contain 'Connection attempt failed'", errOutput.contains("Connection attempt failed: Connection attempt failed"));
    }

    @Test
    public void testReadDataConnectionSuccess() throws URISyntaxException, IOException {
        WebSocketClientReader spyClient = spy(webSocketClientReader);

        doNothing().when(spyClient).connect();

        spyClient.readData(new URI("ws://localhost:8080"), dataStorage);

        verify(spyClient, times(1)).connect();
    }
}
