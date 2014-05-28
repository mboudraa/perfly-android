package com.samantha.app.event;

import android.content.pm.ApplicationInfo;
import android.os.Debug;
import com.samantha.app.core.MemoryInfo;

public class MemoryInfoEvent extends Event{

    public final MemoryInfo memoryInfo;
    public final ApplicationInfo applicationInfo;

    public MemoryInfoEvent(ApplicationInfo appInfo, MemoryInfo memInfo, long time) {
        super(time);
        applicationInfo = appInfo;
        memoryInfo = memInfo;
    }

}
