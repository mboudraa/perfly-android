package com.samantha.app.event;

public abstract class MonitoringEvent {

    public final long time;

    public MonitoringEvent(long time) {
        this.time = time;
    }
}
