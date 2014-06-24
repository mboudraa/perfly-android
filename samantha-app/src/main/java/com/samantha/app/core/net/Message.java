package com.samantha.app.core.net;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.samantha.app.core.json.JsonFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message<T> {

    public final T body;

    public final String address;

    @JsonCreator
    public Message(@JsonProperty("body") T body, @JsonProperty("address") String address) {
        this.body = body;
        this.address = address;
    }

    public String serialize() throws JsonProcessingException {
        return JsonFormatter.toJson(this);
    }
}
