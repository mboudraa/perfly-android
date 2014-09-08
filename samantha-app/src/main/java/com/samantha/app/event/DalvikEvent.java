package com.samantha.app.event;

public class DalvikEvent extends MonitoringEvent {

    public final String type;
    public final String value;

    public DalvikEvent(String type, String value, long time) {
        super(time);
        this.type = type;
        this.value = value;
    }
}
