package com.samantha.app.sys;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.samantha.app.core.ApplicationState;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.samantha.app.core.ApplicationState.State;

public class ApplicationStateWatcher {


    private ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mScheduledFuture;
    private ActivityManager mActivityManager;

    private int mCurrentPid = ApplicationState.PID_NONE;
    private State mCurrentState = State.NOT_RUNNING;
    private Listener mListener;

    ApplicationStateWatcher(Context context) {
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }


    private void stopWatching() {
        mScheduledFuture.cancel(false);
    }


    private void startWatching(final ApplicationInfo applicationInfo) {
        mScheduledFuture = mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ApplicationState state = getApplicationState(applicationInfo);
                long time = new Date().getTime();
                if (mCurrentPid != state.pid) {
                    mListener.onPidChanged(applicationInfo, state.pid, time);
                    mCurrentPid = state.pid;
                }

                if (mCurrentState != state.state) {
                    mListener.onStateChanged(applicationInfo, state.state, time);
                    mCurrentState = state.state;
                }

            }

        }, 0, 1, TimeUnit.SECONDS);
    }


    private ApplicationState getApplicationState(ApplicationInfo applicationInfo) {

        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = mActivityManager.getRunningAppProcesses();

        if (runningAppProcessInfos != null && !runningAppProcessInfos.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfos) {
                if (applicationInfo.processName.equals(processInfo.processName)) {
                    return new ApplicationState(processInfo.pid, State.get(processInfo.importance));
                }
            }
        }

        return new ApplicationState(ApplicationState.PID_NONE, State.NOT_RUNNING);
    }

    public void start(ApplicationInfo applicationInfo, Listener listener) {
        mListener = listener;
        startWatching(applicationInfo);
    }

    public void stop() {
        mListener = null;
        stopWatching();
    }


    public interface Listener {
        void onPidChanged(ApplicationInfo mApplicationInfo, int pid, long time);

        void onStateChanged(ApplicationInfo mApplicationInfo, State state, long time);
    }
}
