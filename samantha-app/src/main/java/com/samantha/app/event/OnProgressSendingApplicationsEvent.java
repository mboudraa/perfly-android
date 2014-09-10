package com.samantha.app.event;

import com.samantha.app.core.sys.Application;

public class OnProgressSendingApplicationsEvent {

    public final Application application;
    public final int progress;
    public final int total;

    public OnProgressSendingApplicationsEvent(Application app, int progress, int total) {
        application = app;
        this.progress = progress;
        this.total = total;
    }
}
