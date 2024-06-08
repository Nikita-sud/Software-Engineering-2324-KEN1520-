package com.alerts.decorator;

import com.alerts.Alert;

/**
 * Decorator class that adds repeated alert functionality.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    /**
     * Constructor that accepts an Alert object to be decorated with repeated alert functionality.
     *
     * @param decoratedAlert the Alert object to be decorated
     */
    public RepeatedAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    /**
     * Method to trigger the alert with added repeated alert functionality.
     * This method first calls the triggerAlert() method of the decorated Alert
     * and then adds additional behavior for repeated alert checks.
     */
    @Override
    public void triggerAlert() {
        super.triggerAlert();
        System.out.println("Repeated alert check.");
    }
}
