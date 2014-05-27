package com.samantha.app.event;

public class ConnectToServerFailureEvent {
    public final Throwable error;

    public ConnectToServerFailureEvent(Throwable throwable) {
        error = throwable;
    }
}
