package com.samantha.app.service.sys;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Debug;
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
                            String.format("/sdcard/samantha/%s/dump/%d.hprof", mPackageName, new Date().getTime()));
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
