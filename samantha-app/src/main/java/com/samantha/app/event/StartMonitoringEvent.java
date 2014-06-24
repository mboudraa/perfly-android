package com.samantha.app.event;

import android.content.Intent;
import android.content.pm.ApplicationInfo;

public class StartMonitoringEvent {
    public final String packageName;

    public StartMonitoringEvent(String packageName) {
        this.packageName = packageName;
    }
}
