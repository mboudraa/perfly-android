package com.samantha.app.event;

import android.content.pm.ApplicationInfo;
import com.samantha.app.core.sys.ApplicationStatus;

public class ApplicationStatusChangedEvent extends MonitoringEvent {

    public final ApplicationInfo applicationInfo;
    public final ApplicationStatus applicationStatus;

    public ApplicationStatusChangedEvent(ApplicationInfo applicationInfo, ApplicationStatus status, long time) {
        super(time);
        applicationStatus = status;
        this.applicationInfo = applicationInfo;
    }
}
