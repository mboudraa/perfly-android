package com.samantha.app.exception;

public class ServiceNotRunningException extends IllegalStateException{

    public ServiceNotRunningException() {
    }

    public ServiceNotRunningException(String detailMessage) {
        super(detailMessage);
    }

    public ServiceNotRunningException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotRunningException(Throwable cause) {
        super(cause);
    }
}
