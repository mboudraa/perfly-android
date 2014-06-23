package com.samantha.app.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JsonFormatter {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonFormatter() {
    }

    public static String toJson(Object o) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(o);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(json, clazz);
    }
}
