package com.samantha.app.event;

public class OrientationChangedEvent extends Event {

    public final int orientation;

    public OrientationChangedEvent(int orientation, long time) {
        super(time);
        this.orientation = orientation;
    }
}
