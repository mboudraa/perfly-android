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
