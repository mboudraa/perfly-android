package com.samantha.app.core.net;

import com.google.gson.annotations.Expose;
import com.samantha.app.core.sys.CpuInfo;
import com.samantha.app.utils.JsonFormatter;

public class CpuInfoMessage implements Message {

    @Expose
    public final CpuInfo cpuInfo;
    @Expose
    public final long time;

    public CpuInfoMessage(CpuInfo cpuInfo, long time) {
        this.cpuInfo = cpuInfo;
        this.time = time;
    }

    @Override
    public String serialize() {
        return JsonFormatter.toJson(this);
    }

    @Override
    public String toString() {
        return serialize();
    }
}
