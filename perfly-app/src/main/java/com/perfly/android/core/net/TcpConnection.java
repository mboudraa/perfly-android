/*
 * Copyright (c) 2014 Mounir Boudraa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perfly.android.core.net;

import android.os.Handler;
import android.os.Looper;
import com.perfly.android.utils.Json;
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
                            String messageString = new String(buffer, 0, readBytes, "UTF-8");
                            notifyMessage(Json.fromJson(messageString, Message.class));
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


    public void sendMessage(final Message message) {
        if (mSocket != null) {
            try {
                PrintWriter writer = new PrintWriter(mSocket.getOutputStream());

                writer.print(message.serialize() + EOC);
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
        if (mListener != null) {
            mListener.onClose();
        }
    }


    private void notifyOpen() {
        if (mListener != null) {
            mListener.onOpen();
        }
    }

    private void notifyError(final Exception e) {
        if (mListener != null) {
            mListener.onError(e);
        }
    }


    private void notifyMessage(final Message message) {
        if (mListener != null) {
            mListener.onMessage(message);
        }
    }


}
