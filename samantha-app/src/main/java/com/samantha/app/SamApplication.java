package com.samantha.app;

import android.app.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import timber.log.Timber;

public class SamApplication extends Application{


    private static SamApplication sInstance;
    private JobManager mJobManager;

    public SamApplication() {
        super();
        sInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configureJobManager();
        configureTimber();
    }

    private void configureTimber(){
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.HollowTree());
        }

    }

    private void configureJobManager() {
        Configuration configuration = new Configuration.Builder(this)
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();
        mJobManager = new JobManager(this, configuration);
    }

    public JobManager getJobManager() {
        return mJobManager;
    }

    public static SamApplication getInstance() {
        return sInstance;
    }
}
