package com.samantha.app.event;

import com.samantha.app.net.Connection;

public class ConnectToServerSuccessEvent {

    public final Connection connection;

    public ConnectToServerSuccessEvent(Connection connection) {
        this.connection = connection;
    }
}
