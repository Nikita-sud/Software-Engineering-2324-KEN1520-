package com.alerts.decorator;

import com.alerts.Alert;

/**
 * Decorator class that adds priority to the alert.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    /**
     * Constructor that accepts an Alert object to be decorated with priority.
     *
     * @param decoratedAlert the Alert object to be decorated
     */
    public PriorityAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    /**
     * Method to trigger the alert with added priority functionality.
     * This method first calls the triggerAlert() method of the decorated Alert
     * and then adds additional behavior for priority alerts.
     */
    @Override
    public void triggerAlert() {
        super.triggerAlert();
        System.out.println("Priority alert triggered.");
    }
}
