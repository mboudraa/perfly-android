package com.perfly.android.event;

public class OnStartSendingApplicationsEvent {

    public final int total;

    public OnStartSendingApplicationsEvent(int total) {
        this.total = total;
    }
}
