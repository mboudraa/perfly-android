package com.samantha.app.event;

import android.content.pm.ApplicationInfo;

public class ApplicationPidChangedEvent extends MonitoringEvent {

    public final ApplicationInfo applicationInfo;
    public final int pid;

    public ApplicationPidChangedEvent(ApplicationInfo applicationInfo, int pid, long time) {
        super(time);
        this.pid = pid;
        this.applicationInfo = applicationInfo;
    }
}
