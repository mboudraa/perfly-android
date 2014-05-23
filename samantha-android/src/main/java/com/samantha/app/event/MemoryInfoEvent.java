package com.samantha.app.event;

import android.content.pm.ApplicationInfo;
import android.os.Debug;
import com.samantha.app.core.MemoryInfo;

public class MemoryInfoEvent {

    public final MemoryInfo memoryInfo;
    public final long time;
    public final ApplicationInfo applicationInfo;

    public MemoryInfoEvent(ApplicationInfo appInfo, MemoryInfo memInfo, long time) {
        applicationInfo = appInfo;
        memoryInfo = memInfo;
        this.time = time;
    }

}
