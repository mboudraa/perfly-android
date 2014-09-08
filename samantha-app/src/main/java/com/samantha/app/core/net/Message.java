package com.samantha.app.core.net;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.samantha.app.core.json.Json;
import timber.log.Timber;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message<T> {

    public final T body;

    public final String address;

    @JsonCreator
    public Message(@JsonProperty("body") T body, @JsonProperty("address") String address) {
        this.body = body;
        this.address = address;
    }

    @Override
    public String toString() {
        try {
            return Json.toJson(this);
        } catch (JsonProcessingException e) {
            Timber.w("Impossible to stringify message");
            return super.toString();
        }
    }

    public String serialize() throws JsonProcessingException {
        return Json.toJson(this);
    }
}
