package com.perfly.android.service.sys;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import hugo.weaving.DebugLog;

public class MonitoringManager {

    private boolean mIsmonitoring;

    private final OrientationEventManager mOrientationEventManager;
    private final DalvikEventManager mDalvikEventManager;
    private final CpuInfoManager mCpuInfoManager;
    private final MemoryInfoManager mMemoryInfoManager;
    private final ApplicationStatusManager mApplicationStatusManager;

    public MonitoringManager(Context context, ApplicationInfo applicationInfo) {
        mDalvikEventManager = new DalvikEventManager(context, applicationInfo);
        mOrientationEventManager = new OrientationEventManager(context, applicationInfo);
        mCpuInfoManager = new CpuInfoManager(context, applicationInfo);
        mMemoryInfoManager = new MemoryInfoManager(context, applicationInfo);
        mApplicationStatusManager = new ApplicationStatusManager(context, applicationInfo);
    }

    @DebugLog
    public void start() {
        if (mIsmonitoring) {
            throw new IllegalStateException("Monitoring must be stopped before starting it");
        }
        mCpuInfoManager.start();
        mMemoryInfoManager.start();
        mOrientationEventManager.start();
        mDalvikEventManager.start();
        mApplicationStatusManager.start();
        mIsmonitoring = true;
    }

    @DebugLog
    public void stop() {
        mIsmonitoring = false;
        mApplicationStatusManager.stop();
        mOrientationEventManager.stop();
        mDalvikEventManager.stop();
        mMemoryInfoManager.stop();
        mCpuInfoManager.stop();
    }

    public boolean isMonitoring() {
        return mIsmonitoring;
    }





}
