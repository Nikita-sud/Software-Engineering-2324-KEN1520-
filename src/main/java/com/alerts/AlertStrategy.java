package com.alerts;

import com.data_management.Patient;

public interface AlertStrategy {
    public void checkAlert(Patient patient);
}
