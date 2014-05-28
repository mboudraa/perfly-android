package com.samantha.app.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.util.Date;

public class SystemEventListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.i("System Event -> %s",intent.getAction());
        EventBus.getDefault().post(new SystemEvent(intent.getAction(), new Date().getTime()));
    }
}
