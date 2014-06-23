package com.samantha.app.core.net;

import android.os.Handler;
import android.os.Looper;
import timber.log.Timber;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;


public class TcpConnection extends Connection {

    private static final String EOC = "||EOC||";
    private Socket mSocket;
    private Thread mReadingThread;
    private Thread mConnectThread;


    private ScheduledExecutorService mStatusSocketScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mStatusFuture;


    Handler mHandler = new Handler(Looper.getMainLooper());


    public TcpConnection(Listener listener) {
        setListener(listener);
    }

    public TcpConnection(String hostname, int port, Listener listener) {
        super(hostname, port, listener);
    }



    public void open() {

        mConnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket(mHostname, mPort);
                    notifyOpen();
                    startReading();
                } catch (SocketTimeoutException e) {
                    notifyError(e);
                } catch (IOException e) {
                    notifyError(e);
                }
            }
        });
        mConnectThread.start();
    }

    private void startReading() {
        mReadingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = mSocket.getInputStream();
                    int readBytes;
                    byte[] buffer = new byte[1024];
                    while ((readBytes = inputStream.read(buffer, 0, 1024)) >= 0) {
                        if (readBytes > 0) {
                            String message = new String(buffer, 0, readBytes, "UTF-8");
                            notifyMessage(message);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    notifyError(e);
                }

            }
        });

        mReadingThread.start();
    }

    public void close() {
        mReadingThread.interrupt();

        try {
            if (mSocket != null && !mSocket.isClosed()) {
                mSocket.close();
            }
        } catch (IOException e) {
            Timber.e(e, "");
        } finally {
            notifyClose();
        }
    }


    public void sendMessage(final Message message, String address) {
        if (mSocket != null) {
            try {
                PrintWriter writer = new PrintWriter(mSocket.getOutputStream());

                writer.print(new MessageWrapper(message, address).serialize() + EOC);
                writer.flush();
            } catch (IOException e) {
                notifyError(e);
            }
        }
    }


    public boolean isOpen() {
        return mSocket != null && !mSocket.isClosed();
    }


    private void notifyClose() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onClose();
            }
        });
    }


    private void notifyOpen() {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
        mListener.onOpen();
//            }
//        });
    }

    private void notifyError(final Exception e) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
                mListener.onError(e);
//            }
//        });
    }


    private void notifyMessage(final String s) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
                mListener.onMessage(s);
//            }
//        });
    }


}