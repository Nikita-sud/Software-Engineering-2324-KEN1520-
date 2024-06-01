package alerts;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the AlertFactory implementations.
 * This class contains test cases to verify the functionality of the alert creation factories.
 */
public class FactoryTest {

    /**
     * Tests the BloodOxygenAlertFactory by creating an alert and verifying its attributes.
     */
    @Test
    public void testBloodOxygenAlertFactory() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("patient123", "low oxygen", 1627844951000L);

        assertNotNull(alert);
        assertEquals("patient123", alert.getPatientId());
        assertEquals("low oxygen", alert.getCondition());
        assertEquals(1627844951000L, alert.getTimestamp());
    }

    /**
     * Tests the BloodPressureAlertFactory by creating an alert and verifying its attributes.
     */
    @Test
    public void testBloodPressureAlertFactory() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("patient456", "high blood pressure", 1627844952000L);

        assertNotNull(alert);
        assertEquals("patient456", alert.getPatientId());
        assertEquals("high blood pressure", alert.getCondition());
        assertEquals(1627844952000L, alert.getTimestamp());
    }

    /**
     * Tests the ECGAlertFactory by creating an alert and verifying its attributes.
     */
    @Test
    public void testECGAlertFactory() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("patient789", "abnormal ECG", 1627844953000L);

        assertNotNull(alert);
        assertEquals("patient789", alert.getPatientId());
        assertEquals("abnormal ECG", alert.getCondition());
        assertEquals(1627844953000L, alert.getTimestamp());
    }
}