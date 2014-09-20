package com.perfly.android.core.net;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.perfly.android.core.json.Json;
import com.perfly.android.core.sys.Device;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import timber.log.Timber;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MQTTConnection extends Connection implements MqttCallback {

    public static final int QOS_DELIVERY_ONCE_NO_CONFIRMATION = 0;
    public static final int QOS_DELIVERY_AT_LEAST_ONCE_WITH_CONFIRMATION = 1;
    public static final int QOS_DELIVERY_ONLY_ONCE_WITH_CONFIRMATION = 2;

    private static final int PORT_DEFAULT = 1883;
    private static final int KEEP_ALIVE_INTERVAL = 5;
    private static final int TIMEOUT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
    private static final String TOPIC_PREFIX = "samantha.vertx";

    private final Device mDevice;
    private final ConcurrentLinkedQueue<Message> mMessageCache = new ConcurrentLinkedQueue<>();
    private final ExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private final MqttConnectOptions mMqttConnectOptions;

    private MqttClient mClient;

    public MQTTConnection(Device device) {
        mDevice = device;
        mMqttConnectOptions = new MqttConnectOptions();
        mMqttConnectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        mMqttConnectOptions.setConnectionTimeout(TIMEOUT);

        try {
            mMqttConnectOptions.setWill(createTopic("device.disconnect"), Json.toByteArray(mDevice),
                                        QOS_DELIVERY_ONCE_NO_CONFIRMATION, true);
        } catch (JsonProcessingException e) {
            //Should not Happen
            throw new RuntimeException(e);
        }
    }

    @Override
    public MQTTConnection setListener(Listener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public MQTTConnection setHostname(String hostname) {
        mHostname = hostname;
        return this;
    }

    @Override
    public MQTTConnection setPort(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException("port should be strictly positive");
        }
        mPort = port;
        return this;
    }

    @Override
    public void open() {


        mExecutor.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    if (isOpen()) {
                        close();
                    }

                    final String uri = String.format("tcp://%s:%d", mHostname, mPort == 0 ? PORT_DEFAULT : mPort);
                    mClient = new MqttClient(uri, mDevice.id, new MemoryPersistence());
                    mClient.setCallback(MQTTConnection.this);
                    mClient.connect(mMqttConnectOptions);
                    mClient.subscribe(mDevice.id + "/#");
                    mClient.publish(createTopic("device.connect"), Json.toByteArray(mDevice),
                                    QOS_DELIVERY_ONCE_NO_CONFIRMATION, true);

                    if (mListener != null) {
                        mListener.onOpen();
                    }
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
//        mMessageCache.add(message);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    while (!mMessageCache.isEmpty()) {
                    Message cachedMessage = message;
                    final Map<String, Object> wrapper = new HashMap<>();
                    wrapper.put("data", cachedMessage.body);
                    wrapper.put("deviceId", mClient.getClientId());

                    final String topic = createTopic(cachedMessage.address);
                    final byte[] payload = Json.toByteArray(wrapper);
                    mClient.publish(topic, payload, QOS_DELIVERY_ONLY_ONCE_WITH_CONFIRMATION, false);
//                        mMessageCache.poll();
//                    }

                } catch (Exception e) {

                    String reason = e.getMessage();
                    if (e instanceof MqttException) {
                        reason = getReason(((MqttException) e).getReasonCode());
                    }

                    Timber.e(e, "Failed to send message cause : %s", reason);
                    if (mListener != null) {
                        mListener.onError(e);
                    }
                }
            }
        });
    }


    public void clearCache() {
        mMessageCache.clear();
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
            Map<String, Object> body = Json.fromByteArray(mqttMessage.getPayload(),
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


    private String createTopic(String address) {
        return String.format("%s/%s/%s", TOPIC_PREFIX, mDevice.id, address);
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