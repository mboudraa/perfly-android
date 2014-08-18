package com.samantha.app.service;

import android.content.Context;
import com.samantha.app.SamApplication;
import com.samantha.app.core.net.Message;
import com.samantha.app.core.sys.Application;
import com.samantha.app.event.*;
import com.samantha.app.job.ListInstalledApplicationsJob;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Map;

public class MessageHandler {

    private Context mContext;
    private EventBus mEventBus = EventBus.getDefault();


    public MessageHandler(Context context) {
        mContext = context;
    }

    public void onMessage(Message message) {
        switch (message.address) {
            case "android.apps.get":
                SamApplication.getInstance()
                              .getJobManager()
                              .addJobInBackground(new ListInstalledApplicationsJob(mContext));
                break;

            case "android.monitoring.start":
                String packageName = ((Map<String, String>) message.body).get("packageName");
                mEventBus.post(new StartMonitoringEvent(packageName));
                break;

            case "android.monitoring.stop":
                mEventBus.post(new StopMonitoringEvent());
                break;

        }
    }

    public void onEvent(ApplicationsInstalledEvent event) {
        sendApplications(event.applications);
    }

    private void sendApplications(ArrayList<Application> applications) {
        mEventBus.post(new OnStartSendingApplicationsEvent(applications.size()));

        for (int i = 0; i < applications.size(); i++) {
            mEventBus.post(
                    new SendMessageEvent(new Message<Application>(applications.get(i), "vertx.app.post")));
            mEventBus.post(new OnProgressSendingApplicationsEvent(applications.get(i), (i + 1)));
        }
        mEventBus.post(new OnFinishSendingApplicationsEvent());

    }


}
