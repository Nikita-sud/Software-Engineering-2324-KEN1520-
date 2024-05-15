import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientReader;

import java.io.IOException;
import java.net.URI;

public class ErrorHandlingTest {
    private DataStorage mockDataStorage;
    private WebSocketClientReader webSocketClientReader;
    private URI serverUri;

    @Before
    public void setUp() throws Exception {
        mockDataStorage = Mockito.mock(DataStorage.class);
        serverUri = new URI("ws://localhost:8080");
        webSocketClientReader = Mockito.spy(new WebSocketClientReader(serverUri, mockDataStorage));
    }

    @Test
    public void testConnectionError() {
        doThrow(new RuntimeException("Connection error")).when(webSocketClientReader).connect();
        try {
            webSocketClientReader.readData(serverUri, mockDataStorage);
            fail("Expected an IOException to be thrown");
        } catch (IOException e) {
            assertEquals("Connection error", e.getCause().getMessage());
        }
    }

    @Test
    public void testReconnectionAfterError() throws Exception {
        doNothing().when(webSocketClientReader).reconnect();
        webSocketClientReader.onError(new Exception("Test error"));
        verify(webSocketClientReader, times(1)).reconnect();
    }

    @Test
    public void testReconnectionAfterClose() throws Exception {
        doNothing().when(webSocketClientReader).reconnect();
        webSocketClientReader.onClose(1000, "Test close", true);
        verify(webSocketClientReader, times(1)).reconnect();
    }
}
