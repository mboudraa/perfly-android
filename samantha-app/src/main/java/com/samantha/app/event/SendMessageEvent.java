package com.samantha.app.event;

import com.samantha.app.core.net.Message;

public class SendMessageEvent {

    public final Message message;

    public SendMessageEvent(Message message) {
        this.message = message;
    }
}
