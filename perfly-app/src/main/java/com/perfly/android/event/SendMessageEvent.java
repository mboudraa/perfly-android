package com.perfly.android.event;

import com.perfly.android.core.net.Message;

public class SendMessageEvent {

    public final Message message;

    public SendMessageEvent(Message message) {
        this.message = message;
    }
}
