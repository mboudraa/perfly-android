/*
 * Copyright (c) 2014 Mounir Boudraa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
