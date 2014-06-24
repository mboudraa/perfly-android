package com.samantha.app.core.net;

import timber.log.Timber;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ServerConnection extends Connection implements Connection.Listener {

    private TcpConnection mServerConnection;
    private TcpConnection mStatusConnection;

    private ScheduledExecutorService mStatusScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mStatusScheduledFuture;

    private ScheduledExecutorService mCLoseScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mCloseScheduledFuture;


    public ServerConnection(Listener listener) {
        mServerConnection = new TcpConnection(listener);
        mStatusConnection = new TcpConnection(this);
    }

    public ServerConnection(String hostname, int port, Listener listener) {
        super(hostname, port, listener);
        mServerConnection = new TcpConnection(hostname, port, listener);
        mStatusConnection = new TcpConnection(hostname, port + 1, this);
    }

    @Override
    public Connection setHostname(String hostname) {
        mServerConnection.setHostname(hostname);
        mStatusConnection.setHostname(hostname);
        return super.setHostname(hostname);
    }

    @Override
    public Connection setPort(int port) {
        mServerConnection.setPort(port);
        mStatusConnection.setPort(port + 1);
        return super.setPort(port);
    }

    @Override
    public Connection setListener(Listener listener) {
        return super.setListener(listener);
    }

    @Override
    public void open() {
        mServerConnection.open();
        mStatusConnection.open();

    }

    @Override
    public void close() {
        mServerConnection.close();
        mStatusConnection.close();
    }

    @Override
    public void sendMessage(Message message) {
        mServerConnection.sendMessage(message);
    }


    @Override
    public boolean isOpen() {
        return mServerConnection.isOpen();
    }

    @Override
    public void onOpen() {

        mStatusScheduledFuture = mStatusScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Timber.v("ping");
                mStatusConnection.sendMessage(new Message<String>(null, "connection.status"));
                mCloseScheduledFuture = mStatusScheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Timber.i("Serveur not responding after 1 second. closing Connection...");
                        close();
                    }
                }, 2, TimeUnit.SECONDS);
            }
        }, 1, 3, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(Message m) {
        Timber.v("ping ok");
        if (mCloseScheduledFuture != null) {
            mCloseScheduledFuture.cancel(true);
        }
    }


    @Override
    public void onClose() {
        if (mStatusScheduler != null) {
            mStatusScheduledFuture.cancel(true);
        }

        if (mCloseScheduledFuture != null) {
            mCloseScheduledFuture.cancel(true);
        }
    }

    @Override
    public void onError(Exception e) {

    }
}
