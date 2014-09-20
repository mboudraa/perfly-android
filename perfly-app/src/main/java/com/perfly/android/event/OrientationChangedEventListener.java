package com.perfly.android.event;

import android.content.Context;
import android.view.OrientationEventListener;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.util.Date;

public class OrientationChangedEventListener extends OrientationEventListener {


    public OrientationChangedEventListener(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        Timber.i("Orientation changed -> %d", orientation);
        EventBus.getDefault().post(new OrientationChangedEvent(orientation, new Date().getTime()));
    }
}
