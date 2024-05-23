package com.alerts.decorator;

import com.alerts.Alert;

public class RepeatedAlertDecorator extends AlertDecorator {

    public RepeatedAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    @Override
    public void triggerAlert() {
        super.triggerAlert();
        System.out.println("Repeated alert check.");
    }
}
