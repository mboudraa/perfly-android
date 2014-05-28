package com.samantha.app.net;

public class ConnectionProperties {

    int mReconnectAttempts = 3;
    int mReconnectInterval = 500;

    public ConnectionProperties() {
    }

    public ConnectionProperties(int reconnectAttempts, int reconnectInterval) {
        mReconnectAttempts = reconnectAttempts;
        mReconnectInterval = reconnectInterval;
    }

    public ConnectionProperties setReconnectAttempts(final int reconnectAttempts) {
        mReconnectAttempts = reconnectAttempts;
        return this;
    }

    public ConnectionProperties setReconnectInterval(final int reconnectInterval) {
        mReconnectInterval = reconnectInterval;
        return this;
    }

    public int getReconnectAttempts() {
        return mReconnectAttempts;
    }

    public int getReconnectInterval() {
        return mReconnectInterval;
    }
}
