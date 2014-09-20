/*
 * Copyright (c) 2014 Mounir Boudraa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perfly.android.service.sys;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.perfly.android.core.sys.ApplicationStatus;
import de.greenrobot.event.EventBus;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.perfly.android.core.sys.ApplicationStatus.State;

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
