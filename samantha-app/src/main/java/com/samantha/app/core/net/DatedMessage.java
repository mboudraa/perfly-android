package com.samantha.app.core.net;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatedMessage<T> extends Message<T> {

    public final long time;

    @JsonCreator
    public DatedMessage(@JsonProperty T body, @JsonProperty long time, @JsonProperty String address) {
        super(body, address);
        this.time = time;
    }

}
