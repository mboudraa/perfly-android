package com.samantha.app.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonFormatter {

    private JsonFormatter(){}

    private static Gson sGson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static String toJson(Object o) {
        return sGson.toJson(o);
    }
}
