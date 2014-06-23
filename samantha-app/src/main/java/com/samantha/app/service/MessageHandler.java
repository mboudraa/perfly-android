package com.samantha.app.service;

import android.content.Context;
import com.samantha.app.SamApplication;
import com.samantha.app.core.net.Message;
import com.samantha.app.core.net.MessageWrapper;
import com.samantha.app.core.sys.Application;
import com.samantha.app.event.*;
import com.samantha.app.job.ListInstalledApplicationsJob;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;

public class MessageHandler {

    private Context mContext;
    private EventBus mEventBus = EventBus.getDefault();


    private int mCurrentIndex;
    private ArrayList<Application> mInstalledApplications = new ArrayList<>();

    public MessageHandler(Context context) {
        mContext = context;
    }

    public void onMessage(MessageWrapper messageWrapper) {
        switch (messageWrapper.address) {
            case "android.apps.get":
                SamApplication.getInstance()
                              .getJobManager()
                              .addJobInBackground(new ListInstalledApplicationsJob(mContext));
                break;

            case "vertx.app.post.ok":
                sendApplication(mCurrentIndex);
                break;
        }
    }

    public void onEvent(ApplicationsInstalledEvent event) {
        mInstalledApplications.clear();
        mInstalledApplications.addAll(event.applications);
//        sendApplication(mCurrentIndex);
        sendAll();
    }

    private void sendAll() {
        mEventBus.post(new OnStartSendingApplicationsEvent(mInstalledApplications.size()));

        for (int i = 0; i < mInstalledApplications.size(); i++) {
            mEventBus.post(
                    new SendMessageEvent(new Message<Application>(mInstalledApplications.get(i)), "vertx.app.post"));
            mEventBus.post(new OnProgressSendingApplicationsEvent(mInstalledApplications.get(i), (i + 1)));
        }
        mEventBus.post(new OnFinishSendingApplicationsEvent());

    }

    private void sendApplication(int index) {

        final int total = mInstalledApplications.size();


        if (mCurrentIndex == 0) {
            mEventBus.post(new OnStartSendingApplicationsEvent(total));

        }
        if (mCurrentIndex < total) {
            final Application app = mInstalledApplications.get(mCurrentIndex);
            mEventBus.post(new SendMessageEvent(new Message<Application>(app), "vertx.app.post"));
            mEventBus.post(new OnProgressSendingApplicationsEvent(app, ++mCurrentIndex));
        } else {
            mEventBus.post(new OnFinishSendingApplicationsEvent());

        }


    }
}
