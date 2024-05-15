package data_management;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class WebSocketClientReaderTest {
    private WebSocketClientReader client;
    private DataStorage mockStorage;

    @Before
    public void setUp() throws URISyntaxException {
        mockStorage = mock(DataStorage.class);
        client = new WebSocketClientReader(new URI("ws://localhost:8080"), mockStorage);
    }

    @Test
    public void testOnOpen() {
        // Set up a stream to capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Call the onOpen method
        ServerHandshake handshake = mock(ServerHandshake.class);
        client.onOpen(handshake);

        // Restore the original System.out
        System.setOut(originalOut);

        // Assert that the output contains the expected string
        assertTrue(outContent.toString().contains("Connected to server"));
    }

    @Test
    public void testOnMessage_validMessage() {
        String message = "Patient ID: 10, Timestamp: 1714748468033, Label: ECG, Data: -0.34656395320945643";
        client.onMessage(message);
        verify(mockStorage).addPatientData(eq(10), eq(-0.34656395320945643), eq("ECG"), eq(1714748468033L));
    }

    @Test
    public void testOnMessage_invalidFormat() {
        String message = "Invalid message format";
        client.onMessage(message);
        verify(mockStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    public void testOnMessage_parsingError() {
        String message = "Patient ID: not_a_number, Timestamp: 1714748468033, Label: ECG, Data: -0.34656395320945643";
        client.onMessage(message);
        verify(mockStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    public void testOnMessage_unexpectedError() {
        WebSocketClientReader spyClient = spy(client);

        // Set up a stream to capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        // Simulate an unexpected error by throwing an exception in addPatientData
        doThrow(new RuntimeException("Unexpected error")).when(mockStorage).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());

        // Call onMessage to trigger the error
        String message = "Patient ID: 10, Timestamp: 1714748468033, Label: ECG, Data: -0.34656395320945643";
        spyClient.onMessage(message);

        // Restore the original System.err
        System.setErr(originalErr);

        // Verify that the error message was printed to System.err
        assertTrue(errContent.toString().contains("Unexpected error: Unexpected error"));
    }


    @Test
    public void testOnClose() {
        client.onClose(1000, "Normal closure", false);
        assertFalse(client.isOpen());
    }

    @Test
    public void testOnCloseWithException() throws IOException {
        WebSocketClientReader spyClient = spy(client);
        
        // Set up a stream to capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        doThrow(new RuntimeException("Reconnection failed")).when(spyClient).reconnect();
        spyClient.onClose(1006, "Abnormal closure", true);

        // Restore the original System.err
        System.setErr(originalErr);

        assertFalse(spyClient.isOpen());
        assertTrue(errContent.toString().contains("Reconnection attempt failed: Reconnection failed"));
    }

    @Test
    public void testOnError() throws InterruptedException {
        WebSocketClientReader spyClient = spy(client);
        CountDownLatch latch = new CountDownLatch(1);

        // Simulate normal reconnection
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(spyClient).reconnect();

        // Set up a stream to capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        Exception testException = new Exception("Test exception");
        spyClient.onError(testException);

        // Wait for the reconnect method to be called
        boolean reconnectCalled = latch.await(5, TimeUnit.SECONDS);

        // Restore the original System.err
        System.setErr(originalErr);

        assertFalse(spyClient.isOpen());
        assertTrue("Reconnect method should be called", reconnectCalled);
        verify(spyClient, atLeastOnce()).reconnect();
        assertTrue(errContent.toString().contains("Test exception"));
    }

    @Test
    public void testOnReconnectAttemptFailure() throws InterruptedException {
        WebSocketClientReader spyClient = spy(client);

        // Set up a stream to capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        // Simulate exception during reconnection
        doThrow(new RuntimeException("Reconnection failed")).when(spyClient).reconnect();

        // Trigger onError to test reconnection exception handling
        Exception testException = new Exception("Test exception");
        spyClient.onError(testException);

        // Wait a bit to ensure the exception is processed
        TimeUnit.SECONDS.sleep(1);

        // Restore the original System.err
        System.setErr(originalErr);

        // Verify that the reconnect method attempted and failed
        verify(spyClient, atLeastOnce()).reconnect();
        assertTrue(errContent.toString().contains("Reconnection attempt failed: Reconnection failed"));
    }

    @Test
    public void testReconnect() throws IOException {
        WebSocketClientReader spyClient = spy(client);
        doNothing().when(spyClient).reconnect();
        spyClient.onClose(1006, "Abnormal closure", true);
        verify(spyClient, atLeastOnce()).reconnect();
    }

    @Test
    public void testReadData_successfulConnection() throws URISyntaxException, IOException {
        WebSocketClientReader spyClient = spy(client);

        // Ensure connect method works without throwing an exception
        doNothing().when(spyClient).connect();

        // Call readData to trigger the connection attempt
        spyClient.readData(new URI("ws://localhost:8080"), mockStorage);

        // Verify that connect was called successfully
        verify(spyClient, times(1)).connect();
    }

    @Test
    public void testReadData_connectionAttemptFailure() throws URISyntaxException, IOException {
        WebSocketClientReader spyClient = spy(client);

        // Set up a stream to capture System.err
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        // Simulate exception during connection
        doThrow(new RuntimeException("Connection attempt failed")).when(spyClient).connect();

        // Call readData to trigger the connection attempt
        spyClient.readData(new URI("ws://localhost:8080"), mockStorage);

        // Restore the original System.err
        System.setErr(originalErr);

        // Verify that the error message was printed to System.err
        assertTrue(errContent.toString().contains("Connection attempt failed: Connection attempt failed"));
    }

}
