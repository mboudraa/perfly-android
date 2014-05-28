package com.samantha.app.sys;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.samantha.app.core.ApplicationState;
import com.samantha.app.event.ApplicationPidChangedEvent;
import com.samantha.app.event.ApplicationStateChangedEvent;
import com.samantha.app.event.SystemEvent;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.samantha.app.core.ApplicationState.State;

public class ApplicationStateWatcher {



    private final ApplicationInfo mApplicationInfo;
    private ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mScheduledFuture;
    private ActivityManager mActivityManager;

    private int mCurrentPid = ApplicationState.PID_NONE;
    private State mCurrentState = State.NOT_RUNNING;

    public ApplicationStateWatcher(Context context, ApplicationInfo applicationInfo) {
        mApplicationInfo = applicationInfo;
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }


    private ScheduledFuture startWatching() {
        return mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ApplicationState state = getApplicationState();
                long time = new Date().getTime();
                if (mCurrentPid != state.pid) {
                    EventBus.getDefault().post(new ApplicationPidChangedEvent(mApplicationInfo, state.pid, time));
                    mCurrentPid = state.pid;
                }

                if(mCurrentState != state.state){
                    EventBus.getDefault().post(new ApplicationStateChangedEvent(mApplicationInfo, state.state, time));
                    mCurrentState = state.state;
                }

            }

        }, 0, 1, TimeUnit.SECONDS);
    }


    private ApplicationState getApplicationState() {

        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = mActivityManager.getRunningAppProcesses();

        if (runningAppProcessInfos != null && !runningAppProcessInfos.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfos) {
                if (mApplicationInfo.processName.equals(processInfo.processName)) {

                    return new ApplicationState(processInfo.pid, State.get(processInfo.importance));
                }
            }
        }

        return new ApplicationState(ApplicationState.PID_NONE, State.NOT_RUNNING);
    }

    public void onEventBackgroundThread(SystemEvent event) {
        Timber.v("%d - %s", event.time, event.action);
    }

    public void start() {
        EventBus.getDefault().register(this);
        mScheduledFuture = startWatching();
    }

    public void stop() {
        mScheduledFuture.cancel(true);
        EventBus.getDefault().unregister(this);
    }

}
