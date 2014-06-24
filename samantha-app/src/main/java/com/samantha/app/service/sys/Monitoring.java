package com.samantha.app.service.sys;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.path.android.jobqueue.JobManager;
import com.samantha.app.SamApplication;
import com.samantha.app.core.net.DatedMessage;
import com.samantha.app.core.sys.ApplicationState;
import com.samantha.app.core.sys.CpuInfo;
import com.samantha.app.event.CpuInfoEvent;
import com.samantha.app.event.MemoryInfoEvent;
import com.samantha.app.event.SendMessageEvent;
import com.samantha.app.job.CpuInfoJob;
import com.samantha.app.job.MemoryInfoJob;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Monitoring implements ApplicationStateWatcher.Listener {

    private Context mContext;
    private boolean mIsmonitoring;
    private ScheduledExecutorService mJobScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mJobScheduledFuture;
    private JobManager mJobManager = SamApplication.getInstance().getJobManager();
    private ApplicationStateWatcher mAppStateWatcher;
    private int mPid = ApplicationState.PID_NONE;
    private EventBus mEventBus = EventBus.getDefault();
    private SystemInfo mSystemInfo = new SystemInfo();

    public Monitoring(Context context) {
        mContext = context.getApplicationContext();
        mAppStateWatcher = new ApplicationStateWatcher(mContext);
    }


    @DebugLog
    public void start(ApplicationInfo applicationInfo) {
        if (applicationInfo == null) {
            throw new NullPointerException("applicationInfo cannot be null");
        }

        if (mIsmonitoring) {
            throw new IllegalStateException("Monitoring must be stopped before starting it");
        }

        mEventBus.register(mSystemInfo);

        mAppStateWatcher.start(applicationInfo, this);
        mIsmonitoring = true;
        mJobScheduledFuture = mJobScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mSystemInfo.dump(mPid);
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
        mEventBus.unregister(mSystemInfo);
    }

    public boolean isMonitoring() {
        return mIsmonitoring;
    }


    @Override
    public void onPidChanged(ApplicationInfo mApplicationInfo, int pid, long time) {
        mPid = pid;
    }

    @Override
    public void onStateChanged(ApplicationInfo mApplicationInfo, ApplicationState.State state, long time) {

    }


    class SystemInfo {

        final AtomicInteger mIncrement;
        long time;
        final ConcurrentHashMap<String, Object> mMap = new ConcurrentHashMap<>();

        SystemInfo() {
            mIncrement = new AtomicInteger(2);
        }

        public void dump(int pid) {
            time = new Date().getTime();
            mMap.put("time", time);
            mJobManager.addJobInBackground(new MemoryInfoJob(mContext, pid, time));
            mJobManager.addJobInBackground(new CpuInfoJob(mContext, pid, time));
        }


        private void post() {
            mEventBus.post(
                    new SendMessageEvent(
                            new DatedMessage<Object>(mMap, time, "android.monitoring.progress")));
        }

        public void onEventBackgroundThread(MemoryInfoEvent event) {

            mMap.put("memoryInfo", event.memoryInfo);
            int result = mIncrement.decrementAndGet();
            if (result == 0) {
                mIncrement.set(2);
                post();
            }

        }

        public void onEventBackgroundThread(CpuInfoEvent event) {
            mMap.put("cpuInfo", event.cpuInfo);
            int result = mIncrement.decrementAndGet();
            if (result == 0) {
                mIncrement.set(2);
                post();
            }
        }
    }
}
