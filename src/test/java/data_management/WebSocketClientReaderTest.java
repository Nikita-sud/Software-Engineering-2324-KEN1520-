package data_management;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientReader;

import java.net.URI;

public class WebSocketClientReaderTest {
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
    public void testOnMessage_ValidMessage() {
        String message = "patientId: 1,timestamp: 1623456789000,label: HeartRate,data: 80";
        webSocketClientReader.onMessage(message);
        verify(mockDataStorage, times(1)).addPatientData(1, 80.0, "HeartRate", 1623456789000L);
    }

    @Test
    public void testOnMessage_InvalidMessageFormat() {
        String message = "invalid,message,format";
        webSocketClientReader.onMessage(message);
        verify(mockDataStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    public void testOnMessage_NumberFormatException() {
        String message = "patientId: abc,timestamp: 1623456789000,label: HeartRate,data: 80";
        webSocketClientReader.onMessage(message);
        verify(mockDataStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    public void testOnError_Reconnect() throws Exception {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                System.out.println("Mock reconnect attempt");
                return null;
            }
        }).when(webSocketClientReader).reconnect();

        webSocketClientReader.onError(new Exception("Test error"));
        verify(webSocketClientReader, times(1)).reconnect();
    }

    @Test
    public void testOnClose_Reconnect() throws Exception {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                System.out.println("Mock reconnect attempt");
                return null;
            }
        }).when(webSocketClientReader).reconnect();

        webSocketClientReader.onClose(1000, "Normal closure", true);
        verify(webSocketClientReader, times(1)).reconnect();
    }
}
