package com.alerts.decorator;

import com.alerts.Alert;

public abstract class AlertDecorator extends Alert {
    protected Alert decoratedAlert;

    public AlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert.getPatientId(), decoratedAlert.getCondition(), decoratedAlert.getTimestamp());
        this.decoratedAlert = decoratedAlert;
    }

    public void triggerAlert() {
        decoratedAlert.triggerAlert();
    }
}
