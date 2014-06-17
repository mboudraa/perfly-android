package com.samantha.app.exception;

public class MonitoringException extends RuntimeException {

    public MonitoringException() {
    }

    public MonitoringException(String detailMessage) {
        super(detailMessage);
    }

    public MonitoringException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MonitoringException(Throwable throwable) {
        super(throwable);
    }
}
