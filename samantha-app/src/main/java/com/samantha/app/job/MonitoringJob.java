package com.samantha.app.job;

import android.content.Context;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

public abstract class MonitoringJob extends BaseJob {

    private final long mTime;

    protected MonitoringJob(Context context, long time) {
        super(context);
        mTime = time;
    }

    protected final long getTime() {
        return mTime;
    }
}
