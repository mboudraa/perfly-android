package com.samantha.app.service.sys;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.path.android.jobqueue.JobManager;
import com.samantha.app.SamApplication;
import com.samantha.app.core.sys.ApplicationState;
import com.samantha.app.event.CpuInfoEvent;
import com.samantha.app.event.MemoryInfoEvent;
import com.samantha.app.event.SendMessageEvent;
import com.samantha.app.job.CpuInfoJob;
import com.samantha.app.job.MemoryInfoJob;
import com.samantha.app.core.net.CpuInfoMessage;
import com.samantha.app.core.net.MemoryInfoMessage;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Monitoring implements ApplicationStateWatcher.Listener {

    private Context mContext;
    private boolean mIsmonitoring;
    private ScheduledExecutorService mJobScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mJobScheduledFuture;
    private JobManager mJobManager = SamApplication.getInstance().getJobManager();
    private ApplicationStateWatcher mAppStateWatcher;
    private int mPid = ApplicationState.PID_NONE;
    private EventBus mEventBus = EventBus.getDefault();

    public Monitoring(Context context) {
        mContext = context.getApplicationContext();
        mAppStateWatcher = new ApplicationStateWatcher(mContext);
    }



    @DebugLog
    public void start(ApplicationInfo applicationInfo) {
        if(applicationInfo == null){
            throw new NullPointerException("applicationInfo cannot be null");
        }

        if(mIsmonitoring){
            throw new IllegalStateException("Monitoring must be stopped before starting it");
        }

        mEventBus.register(this);
        mAppStateWatcher.start(applicationInfo, this);
        mIsmonitoring = true;
        mJobScheduledFuture = mJobScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final long time = new Date().getTime();
                mJobManager.addJobInBackground(new MemoryInfoJob(mContext, mPid, time));
                mJobManager.addJobInBackground(new CpuInfoJob(mContext, mPid, time));
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    @DebugLog
    public void stop() {

        mJobScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (mJobScheduledFuture != null) {
                    mJobScheduledFuture.cancel(true);
                }
            }
        }, 0, TimeUnit.SECONDS);
        mIsmonitoring = false;
        mAppStateWatcher.stop();
        mEventBus.unregister(this);
    }

    public boolean isMonitoring() {
        return mIsmonitoring;
    }


    public void onEventBackgroundThread(MemoryInfoEvent event) {
        mEventBus.post(new SendMessageEvent(new MemoryInfoMessage(event.memoryInfo, event.time)));
    }

    public void onEventBackgroundThread(CpuInfoEvent event) {
        mEventBus.post(new SendMessageEvent(new CpuInfoMessage(event.cpuInfo, event.time)));
    }


    @Override
    public void onPidChanged(ApplicationInfo mApplicationInfo, int pid, long time) {
        mPid = pid;
    }

    @Override
    public void onStateChanged(ApplicationInfo mApplicationInfo, ApplicationState.State state, long time) {

    }
}
