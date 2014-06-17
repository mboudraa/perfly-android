package com.samantha.app.event;

import com.samantha.app.core.MemoryInfo;

public class MemoryInfoEvent extends MonitoringEvent {

    public final MemoryInfo memoryInfo;

    public MemoryInfoEvent(MemoryInfo memInfo, long time) {
        super(time);
        memoryInfo = memInfo;
    }

}
