package com.alerts.decorator;

import com.alerts.Alert;

public class PriorityAlertDecorator extends AlertDecorator {

    public PriorityAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    @Override
    public void triggerAlert() {
        super.triggerAlert();
        System.out.println("Priority alert triggered.");
    }
}
