package com.samantha.app.service.sys;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.samantha.app.core.sys.ApplicationStatus;
import com.samantha.app.event.ApplicationPidChangedEvent;
import com.samantha.app.event.ApplicationStateChangedEvent;
import com.samantha.app.event.ApplicationStatusChangedEvent;
import de.greenrobot.event.EventBus;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.samantha.app.core.sys.ApplicationStatus.State;

public class ProcessEventManager extends AbstractManager {


    private ScheduledExecutorService mScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mScheduledFuture;
    private ActivityManager mActivityManager;

    private int mCurrentPid = ApplicationStatus.PID_NONE;
    private State mCurrentState = State.NOT_RUNNING;
    private EventBus mEventBus = EventBus.getDefault();

    private ApplicationStatus mCurrentStatus;


    ProcessEventManager(Context context, ApplicationInfo applicationInfo) {
        super(context, applicationInfo);
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }


    @Override
    public void start() {
        mScheduledFuture = mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {


            }

        }, 0, 1000, TimeUnit.MILLISECONDS);
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
