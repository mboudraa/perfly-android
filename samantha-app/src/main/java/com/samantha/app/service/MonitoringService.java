package com.samantha.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.samantha.app.R;
import com.samantha.app.activity.MonitoringActivity;
import com.samantha.app.core.net.Connection;
import com.samantha.app.core.net.MQTTConnection;
import com.samantha.app.core.net.Message;
import com.samantha.app.core.sys.Device;
import com.samantha.app.event.*;
import com.samantha.app.exception.MonitoringException;
import com.samantha.app.service.sys.MonitoringManager;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class MonitoringService extends Service implements Connection.Listener {


    public static final String EXTRA_APPLICATION_INFO = "APPLICATION_INFO_EXTRA";
    public static final String EXTRA_HOSTNAME = "EXTRA_HOSTNAME";
    public static final String EXTRA_PORT = "EXTRA_PORT";

    private static final int NOTIFICATION_ID = 0x01;
    private static final int DEFAULT_PORT = 8888;

    private ScheduledExecutorService mSocketScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture mSocketScheduledFuture;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private MQTTConnection mConnection;
    private MonitoringManager mMonitoring;
    private Binder mBinder = new Binder();
    private MessageHandler mMessageHandler;

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
        mMessageHandler = new MessageHandler(this);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this);
        mConnection = new MQTTConnection(Device.getInformations(this)).setListener(this);
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(mMessageHandler);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        final int port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);
        final String hostname = intent.getStringExtra(EXTRA_HOSTNAME);


        if (!isConnectionOpen()) {
            mConnection.setHostname(hostname);
            openConnection();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopMonitoring();
        closeConnection();
        EventBus.getDefault().unregister(mMessageHandler);
        EventBus.getDefault().unregister(this);
    }


    @DebugLog
    public void startMonitoring(String packageName) {

        if (isMonitoring()) {
            stopMonitoring();
        }

        try {
            Intent appToMonitor = getPackageManager().getLaunchIntentForPackage(packageName);
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, 0);

            startForeground(NOTIFICATION_ID, buildNotification(appInfo));
            mMonitoring = new MonitoringManager(this, appInfo);
            mMonitoring.start();
            startActivity(appToMonitor);

        } catch (IllegalStateException | NullPointerException | PackageManager.NameNotFoundException e) {
            throw new MonitoringException(e.getMessage(), e);
        }

    }

    @DebugLog
    public void stopMonitoring() {
        if (isMonitoring()) {
            mMonitoring.stop();
            stopForeground(true);
        }
    }


    public boolean isMonitoring() {
        return mMonitoring != null && mMonitoring.isMonitoring();
    }

    @DebugLog
    public void openConnection() {
        mConnection.open();
    }


    @DebugLog
    public void openConnection(String hostname) {
        mConnection.setHostname(hostname);
        openConnection();
    }


    @DebugLog
    public void closeConnection() {
        mConnection.close();
    }

    public void onEventBackgroundThread(SendMessageEvent event) {
        sendMessage(event.message);
    }

    public void onEvent(StartMonitoringEvent event) {
        startMonitoring(event.packageName);
    }

    public void onEvent(StopMonitoringEvent event) {
        stopMonitoring();
    }


    @DebugLog
    public void sendMessage(Message message) {
//        if (isConnectionOpen()) {
        mConnection.sendMessage(message);
//        }
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

    public boolean isConnectionOpen() {
        return mConnection != null && mConnection.isOpen();
    }

    @Override
    public void onOpen() {
        Timber.i("Socket connected");
        if (mSocketScheduledFuture != null) {
            mSocketScheduledFuture.cancel(true);
        }
        EventBus.getDefault().post(new OnConnectionEvent(true));
    }

    @DebugLog
    @Override
    public void onMessage(Message message) {
        mMessageHandler.onMessage(message);
    }


    @DebugLog
    @Override
    public void onClose() {
        Timber.i("Socket disconnected");
        EventBus.getDefault().post(new OnConnectionEvent(false));
    }

    @Override
    public void onError(Exception error) {
        Timber.w(error, "Socket error");
        EventBus.getDefault().post(new OnConnectionEvent(false));

    }


}
