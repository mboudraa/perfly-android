package com.samantha.app.core.net;


import com.fasterxml.jackson.core.type.TypeReference;
import com.samantha.app.core.json.JsonFormatter;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import timber.log.Timber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MQTTConnection extends Connection implements MqttCallback {

    public static final int QOS_DELIVERY_ONCE_NO_CONFIRMATION = 0;
    public static final int QOS_DELIVERY_AT_LEAST_ONCE_WITH_CONFIRMATION = 1;
    public static final int QOS_DELIVERY_ONLY_ONCE_WITH_CONFIRMATION = 2;

    private static final int KEEP_ALIVE_INTERVAL = 5;

    private static final String CHARSET = "UTF-8";

    public static final int PORT_DEFAULT = 1883;

    ExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    MqttClient mClient;

    public MQTTConnection(Listener listener) {
        mListener = listener;
    }


    public MQTTConnection setHostName(String hostname) {
        mHostname = hostname;
        return this;
    }

    public MQTTConnection setPort(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException("port should be strictly positive");
        }
        mPort = port;
        return this;
    }

    @Override
    public void open() {
        final String uri = String.format("tcp://%s:%d", mHostname, mPort == 0 ? PORT_DEFAULT : mPort);
        try {
            final String clientId = UUID.randomUUID().toString();
            mClient = new MqttClient(uri, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
            mClient.setCallback(this);

            mClient.connect(options);
            mClient.subscribe(clientId + "/#");

            if (mListener != null) {
                mListener.onOpen();
            }
        } catch (MqttException e) {
            Timber.e(e, getReason(e.getReasonCode()));
            if (mListener != null) {
                mListener.onError(e);
            }
        }
    }

    @Override
    public void close() {
        try {
            mClient.disconnect();
            mClient.close();
        } catch (MqttException e) {
            Timber.e(e, getReason(e.getReasonCode()));
            if (mListener != null) {
                mListener.onError(e);
            }
        }

    }

    @Override
    public void sendMessage(final Message message) {
        final Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("data", message.body);
        wrapper.put("deviceId", mClient.getClientId());

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String topic = String.format("samantha.vertx/%s/%s", mClient.getClientId(), message.address);
                    final byte[] payload = JsonFormatter.toByteArray(wrapper);
                    mClient.publish(topic, payload, QOS_DELIVERY_ONLY_ONCE_WITH_CONFIRMATION, false);

                } catch (MqttException e) {
                    Timber.e(e, getReason(e.getReasonCode()));
                    if (mListener != null) {
                        mListener.onError(e);
                    }
                } catch (Exception e) {
                    Timber.e(e, e.getMessage());

                    if (mListener != null) {
                        mListener.onError(e);
                    }
                }
            }
        });


    }

    @Override
    public boolean isOpen() {
        return mClient != null && mClient.isConnected();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Timber.w("Connection lost", throwable);
        if (mListener != null) {
            mListener.onClose();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        if (mListener != null) {
            Map<String, Object> body = JsonFormatter.fromByteArray(mqttMessage.getPayload(),
                                                                   new TypeReference<Map<String, Object>>() {
                                                                   });
            String address = topic.substring(mClient.getClientId().length() + 1);

            Timber.d("message arrive on address %1$s with payload %2$s", address, body);
            mListener.onMessage(new Message(body, address));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    private String getReason(int reasonCode) {
        switch (reasonCode) {
            case MqttException.REASON_CODE_CLIENT_EXCEPTION:
                return "REASON_CODE_CLIENT_EXCEPTION";
            case MqttException.REASON_CODE_INVALID_PROTOCOL_VERSION:
                return "REASON_CODE_INVALID_PROTOCOL_VERSION";
            case MqttException.REASON_CODE_INVALID_CLIENT_ID:
                return "REASON_CODE_INVALID_CLIENT_ID";
            case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                return "REASON_CODE_BROKER_UNAVAILABLE";
            case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                return "REASON_CODE_FAILED_AUTHENTICATION";
            case MqttException.REASON_CODE_NOT_AUTHORIZED:
                return "REASON_CODE_NOT_AUTHORIZED";
            case MqttException.REASON_CODE_UNEXPECTED_ERROR:
                return "REASON_CODE_UNEXPECTED_ERROR";
            case MqttException.REASON_CODE_SUBSCRIBE_FAILED:
                return "REASON_CODE_SUBSCRIBE_FAILED";
            case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                return "REASON_CODE_CLIENT_TIMEOUT";
            case MqttException.REASON_CODE_NO_MESSAGE_IDS_AVAILABLE:
                return "REASON_CODE_NO_MESSAGE_IDS_AVAILABLE";
            case MqttException.REASON_CODE_CLIENT_CONNECTED:
                return "REASON_CODE_CLIENT_CONNECTED";
            case MqttException.REASON_CODE_CLIENT_ALREADY_DISCONNECTED:
                return "REASON_CODE_CLIENT_ALREADY_DISCONNECTED";
            case MqttException.REASON_CODE_CLIENT_DISCONNECTING:
                return "REASON_CODE_CLIENT_DISCONNECTING";
            case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                return "REASON_CODE_SERVER_CONNECT_ERROR";
            case MqttException.REASON_CODE_CLIENT_NOT_CONNECTED:
                return "REASON_CODE_CLIENT_NOT_CONNECTED";
            case MqttException.REASON_CODE_SOCKET_FACTORY_MISMATCH:
                return "REASON_CODE_SOCKET_FACTORY_MISMATCH";
            case MqttException.REASON_CODE_SSL_CONFIG_ERROR:
                return "REASON_CODE_SSL_CONFIG_ERROR";
            case MqttException.REASON_CODE_CLIENT_DISCONNECT_PROHIBITED:
                return "REASON_CODE_CLIENT_DISCONNECT_PROHIBITED";
            case MqttException.REASON_CODE_INVALID_MESSAGE:
                return "REASON_CODE_INVALID_MESSAGE";
            case MqttException.REASON_CODE_CONNECTION_LOST:
                return "REASON_CODE_CONNECTION_LOST";
            case MqttException.REASON_CODE_CONNECT_IN_PROGRESS:
                return "REASON_CODE_CONNECT_IN_PROGRESS";
            case MqttException.REASON_CODE_CLIENT_CLOSED:
                return "REASON_CODE_CLIENT_CLOSED";
            case MqttException.REASON_CODE_TOKEN_INUSE:
                return "REASON_CODE_TOKEN_INUSE";
            case MqttException.REASON_CODE_MAX_INFLIGHT:
                return "REASON_CODE_MAX_INFLIGHT";

            default:
                return "";
        }

    }


}