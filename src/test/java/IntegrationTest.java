import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientReader;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

public class IntegrationTest {
    private DataStorage dataStorage;
    private WebSocketClientReader webSocketClientReader;
    private MockWebSocketServer mockServer;

    @Before
    public void setUp() throws Exception {
        dataStorage = new DataStorage();
        URI serverUri = new URI("ws://localhost:8080");
        webSocketClientReader = new WebSocketClientReader(serverUri, dataStorage);
        mockServer = new MockWebSocketServer(new InetSocketAddress("localhost", 8080));
        mockServer.start();
        webSocketClientReader.connectBlocking();
    }

    @After
    public void tearDown() throws Exception {
        webSocketClientReader.close();
        mockServer.stop();
    }

    @Test
    public void testIntegration() throws Exception {
        mockServer.sendToAll("patientId: 1,timestamp: 1623456789000,label: HeartRate,data: 80");
        Thread.sleep(1000); // Wait for message processing
        List<PatientRecord> records = dataStorage.getRecords(1, 1623456780000L, 1623456790000L);
        assertEquals(1, records.size());
        assertEquals(80.0, records.get(0).getMeasurementValue(), 0.1);

        // Test reconnection logic
        webSocketClientReader.onClose(1000, "Test close", true);
        Thread.sleep(1000); // Wait for reconnection attempt
        assertTrue(webSocketClientReader.isOpen()); // Ensure the connection is re-established
    }

    private class MockWebSocketServer extends WebSocketServer {
        public MockWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            System.out.println("Mock server: connection opened");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Mock server: connection closed");
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            System.out.println("Mock server received message: " + message);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            System.out.println("Mock server error: " + ex.getMessage());
        }

        @Override
        public void onStart() {
            System.out.println("Mock server started");
        }

        public void sendToAll(String text) {
            for (WebSocket conn : getConnections()) {
                conn.send(text);
            }
        }
    }
}
