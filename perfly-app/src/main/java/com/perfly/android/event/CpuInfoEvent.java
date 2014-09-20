package com.perfly.android.event;

import com.perfly.android.core.sys.CpuInfo;

public class CpuInfoEvent extends MonitoringEvent {

    public final CpuInfo cpuInfo;

    public CpuInfoEvent(CpuInfo cpuInfo, long time) {
        super(time);
        this.cpuInfo = cpuInfo;
    }

}
