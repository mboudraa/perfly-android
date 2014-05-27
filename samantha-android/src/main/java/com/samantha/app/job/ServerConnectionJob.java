package com.samantha.app.job;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.samantha.app.event.ConnectToServerFailureEvent;
import com.samantha.app.event.ConnectToServerSuccessEvent;
import com.samantha.app.net.Connection;
import de.greenrobot.event.EventBus;

public class ServerConnectionJob extends Job {


    private final String mHostname;
    private final int mPort;

    public ServerConnectionJob(String hostname, int port) {
        super(new Params(1).setRequiresNetwork(false).setPersistent(false));
        mHostname = hostname;
        mPort = port;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        Connection connection = new Connection(mHostname, mPort);
        EventBus.getDefault().post(new ConnectToServerSuccessEvent(connection));
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        EventBus.getDefault().post(new ConnectToServerFailureEvent(throwable));
        return false;
    }


}
