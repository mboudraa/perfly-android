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
import android.os.Debug;
import android.os.Environment;
import timber.log.Timber;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HeapDumpManager extends AbstractManager {

    ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture mScheduledFuture;
    private final String mPackageName;
    private final long mInterval;

    protected HeapDumpManager(Context context, ApplicationInfo applicationInfo, long interval) {
        super(context, applicationInfo);
        mInterval = interval;
        mPackageName = applicationInfo.packageName;
    }

    @Override
    public void start() {
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                try {
                    Debug.dumpHprofData(

                            String.format("%s/samantha/%s/dump/%d.hprof", Environment.getExternalStorageDirectory().getPath(),mPackageName, new Date().getTime()));
                } catch (IOException e) {
                    Timber.e(e, "dump failed");
                }
            }
        }, 0, mInterval, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {

    }
}
