package com.samantha.app.event;

public class SystemEvent extends Event{

    public final String action;

    public SystemEvent(String action, long time) {
        super(time);
        this.action = action;
    }
}
