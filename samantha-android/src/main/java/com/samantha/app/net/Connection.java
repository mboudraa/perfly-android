package com.samantha.app.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection {
    final Socket mSocket;

    public Connection() {
        mSocket = new Socket();
    }

    public Connection(String hostname, int port) throws UnknownHostException, IOException {
        mSocket = new Socket(hostname, port);
    }


    public void connect(String hostname, int port) throws IOException {
        mSocket.connect(new InetSocketAddress(hostname, port));
    }

    public void sendMessage(Message message) throws IOException {
        if (message == null) {
            throw new NullPointerException("message cannot be null");
        }

        PrintWriter writer = new PrintWriter(mSocket.getOutputStream());
        try {
            writer.write(message.serialize());
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void sendMessage(DataMessage message) throws IOException {
        if (message == null) {
            throw new NullPointerException("message cannot be null");
        }
        byte[] data = message.getData();
        DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
        try {
            final int length = data.length;
            dos.writeInt(length);
            if (length > 0) {
                dos.write(data, 0, length);
            }
            dos.flush();
        } finally {
            if (dos != null) {
                dos.close();
            }
        }
    }

    public void close() throws IOException {
        mSocket.close();
    }
}
