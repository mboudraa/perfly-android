package com.samantha.app.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.samantha.app.net.Message;
import com.samantha.app.utils.JsonFormatter;

public class MemoryInfoMessage implements Message{

    @Expose
    public final MemoryInfo memoryInfo;
    @Expose
    public final long time;



    public MemoryInfoMessage(MemoryInfo memoryInfo, long time) {
        this.memoryInfo = memoryInfo;
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("%d - %s", time, memoryInfo.toString());
    }

    @Override
    public String serialize() {
        return JsonFormatter.toJson(this);
    }
}
