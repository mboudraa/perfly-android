package com.samantha.app.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection {
    final Socket mSocket;
    private PrintWriter mWriter;
    private DataOutputStream mDataoutputStream;

    public Connection() {
        mSocket = new Socket();
    }

    public Connection(String hostname, int port) throws UnknownHostException, IOException {
        mSocket = new Socket(hostname, port);
        mWriter = new PrintWriter(mSocket.getOutputStream());
        mDataoutputStream = new DataOutputStream(mSocket.getOutputStream());

    }


    public void connect(String hostname, int port) throws IOException {
        mSocket.connect(new InetSocketAddress(hostname, port));
        mWriter = new PrintWriter(mSocket.getOutputStream());
        mDataoutputStream = new DataOutputStream(mSocket.getOutputStream());
    }

    public void sendMessage(Message message) throws IOException {
        if (message == null) {
            throw new NullPointerException("message cannot be null");
        }

        mWriter.write(message.serialize());
        mWriter.flush();
    }

    public void sendMessage(DataMessage message) throws IOException {
        if (message == null) {
            throw new NullPointerException("message cannot be null");
        }
        byte[] data = message.getData();
        final int length = data.length;

        mDataoutputStream.writeInt(length);
        if (length > 0) {
            mDataoutputStream.write(data, 0, length);
        }
        mDataoutputStream.flush();

    }

    public void close() throws IOException {
        mWriter.close();
        mDataoutputStream.close();
        mSocket.close();

    }

    public boolean isConnected() {
        return mSocket.isConnected();
    }
}
