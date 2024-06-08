package com.alerts.decorator;

import com.alerts.Alert;

/**
 * Abstract decorator class for Alert, extending the Alert class.
 */
public abstract class AlertDecorator extends Alert {
    /**
     * Field to store the decorated Alert object.
     */
    protected Alert decoratedAlert;

    /**
     * Constructor that accepts a decorated Alert object and passes its parameters to the parent Alert constructor.
     *
     * @param decoratedAlert the Alert object to be decorated
     */
    public AlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert.getPatientId(), decoratedAlert.getCondition(), decoratedAlert.getTimestamp());
        this.decoratedAlert = decoratedAlert;
    }

    /**
     * Method to invoke the triggerAlert() function of the decorated Alert object.
     */
    public void triggerAlert() {
        decoratedAlert.triggerAlert();
    }
}
