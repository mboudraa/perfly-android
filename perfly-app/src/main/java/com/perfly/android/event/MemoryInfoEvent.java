package com.perfly.android.event;

import com.perfly.android.core.sys.MemoryInfo;

public class MemoryInfoEvent extends MonitoringEvent {

    public final MemoryInfo memoryInfo;

    public MemoryInfoEvent(MemoryInfo memInfo, long time) {
        super(time);
        memoryInfo = memInfo;
    }

}
