package com.samantha.app.core.net;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.samantha.app.core.json.JsonFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageWrapper {

    @JsonProperty("body")
    public final Message message;

    public final String address;

    @JsonCreator
    MessageWrapper(@JsonProperty("body") Message message, @JsonProperty("address") String address) {
        this.message = message;
        this.address = address;
    }

    public String serialize() throws JsonProcessingException {
        return JsonFormatter.toJson(this);
    }
}
