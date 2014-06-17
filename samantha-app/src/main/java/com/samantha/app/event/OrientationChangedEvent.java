package com.samantha.app.event;

public class OrientationChangedEvent extends MonitoringEvent {

    public final int orientation;

    public OrientationChangedEvent(int orientation, long time) {
        super(time);
        this.orientation = orientation;
    }
}
