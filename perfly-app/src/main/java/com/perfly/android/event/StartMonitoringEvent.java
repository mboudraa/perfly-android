package com.perfly.android.event;

public class StartMonitoringEvent {
    public final String packageName;

    public StartMonitoringEvent(String packageName) {
        this.packageName = packageName;
    }
}
