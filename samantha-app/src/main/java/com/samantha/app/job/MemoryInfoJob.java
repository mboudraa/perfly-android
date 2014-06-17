package com.samantha.app.job;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import com.samantha.app.core.MemoryInfo;
import com.samantha.app.event.MemoryInfoEvent;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class MemoryInfoJob extends MonitoringJob {


    private final ActivityManager mActivityManager;
    private final int mPid;
    private int mDalvikLimit;


    public MemoryInfoJob(Context context, int pid, long time) {
        super(context, time);
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mDalvikLimit = mActivityManager.getMemoryClass() * 1024;
        mPid = pid;
    }

    @Override
    public void onRun() throws Throwable {
        Debug.MemoryInfo[] infos = mActivityManager.getProcessMemoryInfo(new int[]{mPid});

        if (infos != null && infos.length > 0) {
            Debug.MemoryInfo info = infos[0];

            int appTotal = info.getTotalPrivateDirty() + info.getTotalSharedDirty();
            int appDalvik = info.dalvikPrivateDirty + info.dalvikSharedDirty;
            int appNative = appTotal - appDalvik;

            MemoryInfo memoryInfo = new MemoryInfo(mDalvikLimit, appTotal, appDalvik);
            EventBus.getDefault().post(new MemoryInfoEvent(memoryInfo, getTime()));
        }

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Timber.w(throwable, "");
        return false;
    }
}
