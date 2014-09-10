package com.samantha.app.service;

import android.content.Context;
import com.samantha.app.SamApplication;
import com.samantha.app.core.net.Message;
import com.samantha.app.core.sys.Application;
import com.samantha.app.event.*;
import com.samantha.app.job.ListInstalledApplicationsJob;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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


    public void onEventBackgroundThread(MemoryInfoEvent event) {
        Message message = new Message(event, "android.monitoring.progress/monitoring");
        mEventBus.post(new SendMessageEvent(message));
    }

    public void onEventBackgroundThread(CpuInfoEvent event) {
        Message message = new Message(event, "android.monitoring.progress/monitoring");
        mEventBus.post(new SendMessageEvent(message));
    }

    public void onEventBackgroundThread(ApplicationStatusChangedEvent event) {
        Message message = new Message(event, "android.monitoring.progress/status");
        mEventBus.post(new SendMessageEvent(message));
    }

    public void onEventBackgroundThread(OrientationChangedEvent event) {
        Message message = new Message(event, "android.monitoring.progress/orientation");
        mEventBus.post(new SendMessageEvent(message));
    }

    public void onEventBackgroundThread(DalvikEvent event) {
        Message message = new Message(event, "android.monitoring.progress/dalvik");
        mEventBus.post(new SendMessageEvent(message));
    }

    public void onEventBackgroundThread(ApplicationsInstalledEvent event) {
        sendApplications(event.applications);
    }


    private void sendApplications(ArrayList<Application> applications) {
        final int total = applications.size();
        OnStartSendingApplicationsEvent onStartSendingApplicationsEvent = new OnStartSendingApplicationsEvent(
                total);
        mEventBus.post(new SendMessageEvent(new Message(onStartSendingApplicationsEvent, "android.apps.start")));
        mEventBus.post(onStartSendingApplicationsEvent);

        Collections.sort(applications, new Comparator<Application>() {
            @Override
            public int compare(Application lhs, Application rhs) {
                return lhs.label.compareTo(rhs.label);
            }
        });

        for (int i = 0; i < total; i++) {

            OnProgressSendingApplicationsEvent onProgressSendingApplicationsEvent =
                    new OnProgressSendingApplicationsEvent(applications.get(i), (i + 1), total);
            mEventBus.post(
                    new SendMessageEvent(new Message(onProgressSendingApplicationsEvent, "android.apps.progress")));
            mEventBus.post(onProgressSendingApplicationsEvent);
        }

        mEventBus.post(new SendMessageEvent(new Message(null, "android.apps.finish")));
        mEventBus.post(new OnFinishSendingApplicationsEvent());

    }


}
