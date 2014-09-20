package com.perfly.android.core.net;

public abstract class Connection {

    protected  String mHostname;
    protected  int mPort;
    protected  Listener mListener;

    public abstract void open();

    public abstract void close();

    public abstract void sendMessage(Message message);

    public abstract boolean isOpen();

    public Connection setHostname(final String hostname) {
        mHostname = hostname;
        return this;
    }

    public Connection setPort(final int port) {
        mPort = port;
        return this;
    }

    public Connection setListener(final Listener listener) {
        mListener = listener;
        return this;
    }


    public static interface Listener {
        void onOpen();

        void onMessage(Message s);

        void onClose();

        void onError(Exception e);
    }
}
