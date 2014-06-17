package com.samantha.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.samantha.app.R;
import com.samantha.app.activity.MonitoringActivity;
import com.samantha.app.event.SendMessageEvent;
import com.samantha.app.exception.MonitoringException;
import com.samantha.app.core.net.Message;
import com.samantha.app.service.sys.Monitoring;
import com.samantha.app.core.net.socket.WebSocketClient;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.samantha.app.SamApplication.*;

public class MonitoringService extends Service implements WebSocketClient.Listener {


    public static final String EXTRA_APPLICATION_INFO = "APPLICATION_INFO_EXTRA";
    public static final String EXTRA_HOSTNAME = "EXTRA_HOSTNAME";
    public static final String EXTRA_PORT = "EXTRA_PORT";

    private static final int NOTIFICATION_ID = 0x01;
    private static final int DEFAULT_PORT = 8888;

    private ScheduledExecutorService mSocketScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mSocketScheduledFuture;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private WebSocketClient mWebSocketClient;
    private boolean mSocketConnected;
    private Monitoring mMonitoring;
    private Binder mBinder = new Binder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class Binder extends android.os.Binder {
        public MonitoringService getService() {
            return MonitoringService.this;
        }
    }


    @Override
    public void onCreate() {

        super.onCreate();
        EventBus.getDefault().register(this);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this);
        mMonitoring = new Monitoring(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        final ApplicationInfo appInfo = intent.getParcelableExtra(EXTRA_APPLICATION_INFO);
        final int port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);
        final String hostname = intent.getStringExtra(EXTRA_HOSTNAME);


        startConnection(hostname, port);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        stopMonitoring();
        closeConnection();
    }


    @DebugLog
    public void startMonitoring(ApplicationInfo appInfo) {

        try {
            startForeground(NOTIFICATION_ID, buildNotification(appInfo));
            mMonitoring.start(appInfo);
        } catch (IllegalStateException | NullPointerException e) {
            throw new MonitoringException(e.getMessage(), e);
        }

    }

    @DebugLog
    public void stopMonitoring() {
        if (mMonitoring.isMonitoring()) {
            mMonitoring.stop();
            stopForeground(true);
        }
    }


    public boolean isMonitoring() {
        return mMonitoring.isMonitoring();
    }

    @DebugLog
    public void startConnection(final String hostname, final int port) {

        if (!mSocketConnected) {
            final String wsuri = String.format("ws://%s:%d", hostname, port);
            mWebSocketClient = new WebSocketClient(URI.create(wsuri), this, null);
            mWebSocketClient.connect();
        }
    }

    @DebugLog
    public void closeConnection() {
        if (mWebSocketClient != null) {
            mWebSocketClient.disconnect();
        }
    }

    public void onEventBackgroundThread(SendMessageEvent event) {
        sendMessage(event.message);
    }

    @DebugLog
    public void sendMessage(Message message) {
        if (mSocketConnected) {
            mWebSocketClient.send(message.serialize());
        }
    }

    private Notification buildNotification(ApplicationInfo appInfo) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Monitoring " + getPackageManager().getApplicationLabel(appInfo));

        Intent activityIntent = new Intent(this, MonitoringActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent,
                                                                PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder
                .setContentTitle("Monitoring " + getPackageManager().getApplicationLabel(appInfo))
                .setSmallIcon(R.drawable.ic_launcher)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent);


        return mNotificationBuilder.build();
    }

    @DebugLog
    @Override
    public void onConnect() {
        Timber.i("Socket connected");
        mSocketConnected = true;
        if (mSocketScheduledFuture != null) {
            mSocketScheduledFuture.cancel(true);
        }
    }

    @DebugLog
    @Override
    public void onMessage(String messageString) {
        try {
            Message message = OBJECT_MAPPER.readValue(messageString, Message.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(byte[] data) {

    }

    @DebugLog
    @Override
    public void onDisconnect(int code, String reason) {
        mSocketConnected = false;
        Timber.i("Socket disconnected");
    }

    @Override
    public void onError(Exception error) {
        if (error instanceof IOException) {
            mSocketConnected = false;
            Timber.i("Socket disconnected");
        } else {
            Timber.w(error, "WebSocket error");
        }

    }
}
