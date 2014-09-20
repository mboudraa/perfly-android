package com.perfly.android.service.sys;

import android.content.Context;
import android.content.pm.ApplicationInfo;

abstract class AbstractManager {

    protected final Context mContext;
    protected final ApplicationInfo mApplicationInfo;

    protected AbstractManager(Context context, ApplicationInfo applicationInfo) {

        if (applicationInfo == null) {
            throw new NullPointerException("applicationInfo cannot be null");
        }

        mContext = context.getApplicationContext();
        mApplicationInfo = applicationInfo;

    }

    public abstract void start();

    public abstract void stop();
}
