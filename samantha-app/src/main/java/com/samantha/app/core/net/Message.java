package com.samantha.app.core.net;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.samantha.app.core.json.JsonFormatter;
import timber.log.Timber;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message<T> {

    public final T object;

    @JsonCreator
    public Message(@JsonProperty T object) {
        this.object = object;
    }

    @Override
    public String toString() {
        try {
            return JsonFormatter.toJson(this);
        } catch (JsonProcessingException e) {
            Timber.w(e, "Impossible to stringify object. Returned super.toString().");
            return super.toString();
        }
    }
}
