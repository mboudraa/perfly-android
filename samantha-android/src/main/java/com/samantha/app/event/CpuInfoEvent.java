package com.samantha.app.event;

import android.content.pm.ApplicationInfo;
import com.samantha.app.core.CpuInfo;
import com.samantha.app.core.MemoryInfo;

public class CpuInfoEvent {

    public final CpuInfo cpuInfo;
    public final long time;
    public final ApplicationInfo applicationInfo;

    public CpuInfoEvent(ApplicationInfo appInfo, CpuInfo cpuInfo, long time) {
        applicationInfo = appInfo;
        this.cpuInfo = cpuInfo;
        this.time = time;
    }

}
