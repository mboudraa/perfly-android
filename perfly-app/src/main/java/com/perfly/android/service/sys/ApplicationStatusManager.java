package com.perfly.android.service.sys;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.perfly.android.core.sys.ApplicationStatus;
import com.perfly.android.event.ApplicationPidChangedEvent;
import com.perfly.android.event.ApplicationStateChangedEvent;
import com.perfly.android.event.ApplicationStatusChangedEvent;
import de.greenrobot.event.EventBus;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.perfly.android.core.sys.ApplicationStatus.State;

public class ApplicationStatusManager extends AbstractManager {


    private ScheduledExecutorService mScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mScheduledFuture;
    private ActivityManager mActivityManager;

    private int mCurrentPid = ApplicationStatus.PID_NONE;
    private State mCurrentState = State.NOT_RUNNING;
    private ApplicationStatus mCurrentStatus;


    private EventBus mEventBus = EventBus.getDefault();



    ApplicationStatusManager(Context context, ApplicationInfo applicationInfo) {
        super(context, applicationInfo);
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }


    @Override
    public void start() {
        mScheduledFuture = mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ApplicationStatus status = getApplicationState(mApplicationInfo);
                long time = new Date().getTime();
                if (mCurrentPid != status.pid) {
                    mEventBus.post(new ApplicationPidChangedEvent(mApplicationInfo, status.pid, time));
                    mCurrentPid = status.pid;
                }

                if (mCurrentState != status.state) {
                    mEventBus.post(new ApplicationStateChangedEvent(mApplicationInfo, status.state, time));
                    mCurrentState = status.state;
                }

                if (mCurrentStatus == null || !mCurrentStatus.equals(status)) {
                    mCurrentStatus = status;
                    mEventBus.post(new ApplicationStatusChangedEvent(mApplicationInfo, status, time));
                }

            }

        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        mScheduler.shutdown();
        mScheduledFuture.cancel(false);
    }

    private ApplicationStatus getApplicationState(ApplicationInfo applicationInfo) {

        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = mActivityManager.getRunningAppProcesses();

        if (runningAppProcessInfos != null && !runningAppProcessInfos.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfos) {
                if (applicationInfo.processName.equals(processInfo.processName)) {
                    return new ApplicationStatus(processInfo.pid, State.get(processInfo.importance));
                }
            }
        }

        return new ApplicationStatus(ApplicationStatus.PID_NONE, State.NOT_RUNNING);
    }

}
