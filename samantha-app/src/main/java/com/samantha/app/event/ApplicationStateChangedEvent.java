package com.samantha.app.event;

import android.content.pm.ApplicationInfo;
import com.samantha.app.core.ApplicationState;

public class ApplicationStateChangedEvent extends Event {

    public final ApplicationInfo applicationInfo;
    public final ApplicationState.State state;

    public ApplicationStateChangedEvent(ApplicationInfo applicationInfo, ApplicationState.State state, long time) {
        super(time);
        this.applicationInfo = applicationInfo;
        this.state = state;
    }
}
