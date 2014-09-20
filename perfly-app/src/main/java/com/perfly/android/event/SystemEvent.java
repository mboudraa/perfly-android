package com.perfly.android.event;

public class SystemEvent extends MonitoringEvent {

    public final String action;

    public SystemEvent(String action, long time) {
        super(time);
        this.action = action;
    }
}
