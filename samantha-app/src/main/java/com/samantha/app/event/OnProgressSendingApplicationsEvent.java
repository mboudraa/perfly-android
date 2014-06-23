package com.samantha.app.event;

import com.samantha.app.core.sys.Application;

public class OnProgressSendingApplicationsEvent {

    public final Application application;
    public final int progress;

    public OnProgressSendingApplicationsEvent(Application app, int progress) {
        application = app;
        this.progress = progress;
    }
}
