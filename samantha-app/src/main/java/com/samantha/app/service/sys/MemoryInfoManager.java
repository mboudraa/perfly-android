package com.samantha.app.service.sys;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Debug;
import com.samantha.app.core.sys.ApplicationStatus;
import com.samantha.app.core.sys.MemoryInfo;
import com.samantha.app.event.ApplicationPidChangedEvent;
import com.samantha.app.event.MemoryInfoEvent;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MemoryInfoManager extends AbstractManager {

    private final ActivityManager mActivityManager;
    private final int mDalvikLimit;
    ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture mScheduledFuture;
    EventBus mEventBus = EventBus.getDefault();
    private int mPid = ApplicationStatus.PID_NONE;


    MemoryInfoManager(Context context, ApplicationInfo applicationInfo) {
        super(context, applicationInfo);
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mDalvikLimit = mActivityManager.getMemoryClass() * 1024;
    }

    @Override
    public void start() {
        mEventBus.register(this);
        mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    long time = new Date().getTime();
                    MemoryInfo memoryInfo = dump();
                    if (memoryInfo != null) {
                        mEventBus.post(new MemoryInfoEvent(memoryInfo, time));
                    }
                } catch (Exception e) {
                    Timber.e(e, "Dump Memory Info failed");
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop() {
        mScheduledExecutorService.shutdown();
        mScheduledFuture.cancel(true);
        mEventBus.unregister(this);
    }

    public void onEventBackgroundThread(ApplicationPidChangedEvent event) {
        mPid = event.pid;
    }

    public MemoryInfo dump() {

        int appTotal = 0;
        int appDalvik = 0;
        long allocatedHeapSize = Debug.getNativeHeapAllocatedSize() / 1024;
        long heapSize = Debug.getNativeHeapSize() / 1024;

        Debug.MemoryInfo[] infos = mActivityManager.getProcessMemoryInfo(new int[]{mPid});

        if (infos != null && infos.length > 0) {
            Debug.MemoryInfo info = infos[0];

            appTotal = info.getTotalPrivateDirty() + info.getTotalSharedDirty();
            appDalvik = info.dalvikPrivateDirty + info.dalvikSharedDirty;

        }

        return new MemoryInfo(mDalvikLimit, appTotal, appDalvik, allocatedHeapSize, heapSize);
    }


}