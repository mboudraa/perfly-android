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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import com.perfly.android.event.OrientationChangedEvent;
import de.greenrobot.event.EventBus;

import java.util.Date;

public class OrientationEventManager extends AbstractManager {


    private EventBus mEventBus = EventBus.getDefault();
    private IntentFilter mOrientationIntentFilter;
    private BroadcastReceiver mOrientationBroadcastReceiver;
    private int mOrientation;

    OrientationEventManager(Context context, ApplicationInfo applicationInfo) {
        super(context, applicationInfo);
        mOrientationIntentFilter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
        mOrientationBroadcastReceiver = new OrientationBroadcastReceiver();
    }

    @Override
    public void start() {
        mContext.registerReceiver(mOrientationBroadcastReceiver, mOrientationIntentFilter);
    }

    @Override
    public void stop() {
        mContext.unregisterReceiver(mOrientationBroadcastReceiver);
    }

    class OrientationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long time = new Date().getTime();
            final int orientation = context.getResources().getConfiguration().orientation;

            if (mOrientation != orientation) {
                mOrientation = orientation;
                mEventBus.post(new OrientationChangedEvent(orientation, time));
            }

        }
    }
}
