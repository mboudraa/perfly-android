package com.samantha.app.job;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Debug;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.samantha.app.core.MemoryInfo;
import com.samantha.app.event.MemoryInfoEvent;
import de.greenrobot.event.EventBus;

import java.io.FileDescriptor;
import java.util.Date;

public class MemoryInfoJob extends Job {


    private final ApplicationInfo mApplicationInfo;
    private final ActivityManager mActivityManager;
    private final int mPid;
    private int mDalvikLimit;


    public MemoryInfoJob(Context context, int pid, ApplicationInfo appInfo) {
        super(new Params(1).setRequiresNetwork(false).setPersistent(false));
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mDalvikLimit = mActivityManager.getMemoryClass() * 1024;

        mPid = pid;
        mApplicationInfo = appInfo;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Debug.MemoryInfo[] infos = mActivityManager.getProcessMemoryInfo(new int[]{mPid});

        long time = new Date().getTime();
        if (infos != null && infos.length > 0) {
            Debug.MemoryInfo info = infos[0];

            int appTotal = info.getTotalPrivateDirty() + info.getTotalSharedDirty();
            int appDalvik = info.dalvikPrivateDirty + info.dalvikSharedDirty;
            int appNative = appTotal - appDalvik;

            MemoryInfo memoryInfo = new MemoryInfo(mDalvikLimit, appTotal, appDalvik);
            EventBus.getDefault().post(new MemoryInfoEvent(mApplicationInfo, memoryInfo, time));
        }

    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
