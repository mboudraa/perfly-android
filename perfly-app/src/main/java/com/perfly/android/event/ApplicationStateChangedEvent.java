package com.perfly.android.event;

import android.content.pm.ApplicationInfo;
import com.perfly.android.core.sys.ApplicationStatus;

public class ApplicationStateChangedEvent extends MonitoringEvent {

    public final ApplicationInfo applicationInfo;
    public final ApplicationStatus.State state;

    public ApplicationStateChangedEvent(ApplicationInfo applicationInfo, ApplicationStatus.State state, long time) {
        super(time);
        this.applicationInfo = applicationInfo;
        this.state = state;
    }
}
