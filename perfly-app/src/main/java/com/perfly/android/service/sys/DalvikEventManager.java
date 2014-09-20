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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.perfly.android.core.sys.ApplicationStatus;
import com.perfly.android.event.ApplicationPidChangedEvent;
import com.perfly.android.event.DalvikEvent;
import com.perfly.android.service.LogReader;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DalvikEventManager extends AbstractManager {

    private int mPid = ApplicationStatus.PID_NONE;
    private EventBus mEventBus = EventBus.getDefault();
    ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture mScheduledFuture;

    DalvikEventManager(Context context, ApplicationInfo applicationInfo) {
        super(context, applicationInfo);
    }


    @Override
    public void start() {
        mEventBus.register(this);

        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mPid != ApplicationStatus.PID_NONE) {
                    LogReader.readLog(
                            String.format("dalvikvm:D | grep \"dalvikvm(%d)\"", mPid), new LogReader.LogCallback() {
                                @Override
                                public void onLogOutput(String line) {
                                    Timber.i("Dalvik Event : %s", line.replace(String.valueOf(mPid), ""));
                                    Pattern pattern = Pattern.compile(".{1}/dalvikvm([^:]*): *([^ ]*) (.*)");
                                    Matcher matcher = pattern.matcher(line);


                                    if (matcher.matches() && matcher.groupCount() >= 2
                                            && matcher.group(2).startsWith("GC_")) {
                                        String type = matcher.group(2);
                                        String value = matcher.group(3);
                                        mEventBus.post(new DalvikEvent(type, value, new Date().getTime()));

                                    }

                                }
                            });
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop() {
        mEventBus.unregister(this);
    }

    public void onEventBackgroundThread(ApplicationPidChangedEvent event) {
        mPid = event.pid;
    }

}
