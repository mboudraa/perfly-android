package com.samantha.app.event;

import com.samantha.app.net.CpuInfoMessage;
import com.samantha.app.net.Message;

public class SendMessageEvent {

    public final Message message;

    public SendMessageEvent(Message message) {
        this.message = message;
    }
}
