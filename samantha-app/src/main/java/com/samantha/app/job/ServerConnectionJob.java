package com.samantha.app.job;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.samantha.app.event.ConnectToServerFailureEvent;
import com.samantha.app.event.ConnectToServerSuccessEvent;
import com.samantha.app.net.Connection;
import com.samantha.app.net.ConnectionProperties;
import de.greenrobot.event.EventBus;

public class ServerConnectionJob extends Job {


    private final String mHostname;
    private final int mPort;
    private final int mReconnectAttempts;
    private final int mReconnectInterval;
    private int mCurrentTry = 0;

    public ServerConnectionJob(String hostname, int port) {
        this(hostname, port, new ConnectionProperties());
    }

    public ServerConnectionJob(String hostname, int port, ConnectionProperties connectionProperties) {
        super(new Params(Priority.HIGH).setRequiresNetwork(false).setPersistent(false).delayInMs(0));
        mReconnectAttempts = connectionProperties.getReconnectAttempts();
        mReconnectInterval = connectionProperties.getReconnectInterval();
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
        final boolean retry = getCurrentRunCount() < mReconnectAttempts;
        if (!retry) {
            EventBus.getDefault().post(new ConnectToServerFailureEvent(throwable));
        } else {
            try {
                Thread.sleep(mReconnectInterval);
            } catch (InterruptedException e) {
                //Should never Happen
            }
        }
        return retry;
    }


}
