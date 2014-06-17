package com.samantha.app.event;

import com.samantha.app.core.CpuInfo;

public class CpuInfoEvent extends MonitoringEvent {

    public final CpuInfo cpuInfo;

    public CpuInfoEvent(CpuInfo cpuInfo, long time) {
        super(time);
        this.cpuInfo = cpuInfo;
    }

}
