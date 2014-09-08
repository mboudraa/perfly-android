package com.samantha.app.service.sys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import com.samantha.app.event.OrientationChangedEvent;
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
