package com.perfly.android.job;

import android.content.Context;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

public abstract class BaseJob extends Job {

    private final Context mContext;

    protected BaseJob(Context context) {
        super(new Params(Priority.NORMAL).setRequiresNetwork(false).setPersistent(false).delayInMs(0));
        mContext = context.getApplicationContext();
    }

    @Override
    public void onAdded() {}

    @Override
    protected void onCancel() {}


    protected Context getContext(){
        return mContext;
    }

}
