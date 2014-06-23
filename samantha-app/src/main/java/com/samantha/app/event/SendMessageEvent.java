package com.samantha.app.event;

import com.samantha.app.core.net.Message;

public class SendMessageEvent {

    public final Message message;
    public final String address;

    public SendMessageEvent(Message message, String address) {
        this.message = message;
        this.address = address;
    }
}
