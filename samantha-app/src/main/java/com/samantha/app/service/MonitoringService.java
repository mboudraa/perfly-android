package com.samantha.app.service;

import android.app.*;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.path.android.jobqueue.JobManager;
import com.samantha.app.R;
import com.samantha.app.SamApplication;
import com.samantha.app.activity.MonitoringActivity;
import com.samantha.app.core.ApplicationState;
import com.samantha.app.core.CpuInfoMessage;
import com.samantha.app.core.MemoryInfoMessage;
import com.samantha.app.event.*;
import com.samantha.app.job.CpuInfoJob;
import com.samantha.app.job.MemoryInfoJob;
import com.samantha.app.job.ServerConnectionJob;
import com.samantha.app.net.Connection;
import com.samantha.app.net.Message;
import com.samantha.app.sys.ApplicationStateWatcher;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MonitoringService extends Service {


    public static final String EXTRA_APPLICATION_INFO = "APPLICATION_INFO_EXTRA";
    public static final String EXTRA_HOSTNAME = "EXTRA_HOSTNAME";
    public static final String EXTRA_PORT = "EXTRA_PORT";

    private static final int NOTIFICATION_ID = 0x01;
    private static final int DEFAULT_PORT = 8888;

    Connection mConnection = new Connection();

    ActivityManager mActivityManager;
    ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture mMonitoringHandler;
    JobManager mJobManager = SamApplication.getInstance().getJobManager();
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mNotificationBuilder;
    String mHostname;
    int mPort;
    ApplicationStateWatcher mAppStateWatcher;
    int mPid = ApplicationState.PID_NONE;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("STARTING MONITORING SERVICE");
        EventBus.getDefault().register(this);
        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        ApplicationInfo appInfo = intent.getParcelableExtra(EXTRA_APPLICATION_INFO);

        mPort = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);
        mHostname = intent.getStringExtra(EXTRA_HOSTNAME);
        mAppStateWatcher = new ApplicationStateWatcher(this, appInfo);

        startConnection(mHostname, mPort);
        startForeground(NOTIFICATION_ID, buildNotification(appInfo));
        startMonitoring();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.i("STOPPING MONITORING SERVICE");
        EventBus.getDefault().unregister(this);
        mJobManager.clear();
        stopMonitoring();
        stopForeground(true);
        closeConnection();

    }

    @DebugLog
    public void startConnection(String hostname, int port) {
        mJobManager.addJobInBackground(new ServerConnectionJob(hostname, port));
    }

    @DebugLog
    public void closeConnection() {
        try {
            if (mConnection != null && mConnection.isConnected()) {
                mConnection.close();
            }
        } catch (IOException e) {
            Timber.e("", e);
        }
    }

    @DebugLog
    public void startMonitoring() {
        mAppStateWatcher.start();
        mMonitoringHandler = mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mJobManager.addJobInBackground(new MemoryInfoJob(MonitoringService.this, mPid));
                mJobManager.addJobInBackground(new CpuInfoJob(MonitoringService.this, mPid));
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    @DebugLog
    public void stopMonitoring() {
        mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (mMonitoringHandler != null) {
                    mMonitoringHandler.cancel(true);
                }
            }
        }, 0, TimeUnit.SECONDS);
        mAppStateWatcher.stop();
    }

    public void onEventMainThread(ConnectToServerSuccessEvent event) {
        mConnection = event.connection;
        Timber.i("connextion to server successful");
    }

    public void onEventMainThread(ConnectToServerFailureEvent event) {
        Timber.i("connexion to server failure --> %s", event.error.getCause());
        Toast.makeText(this, "Impossible to connect to server", Toast.LENGTH_LONG).show();
        stopSelf();

    }

    public void onEventBackgroundThread(ApplicationPidChangedEvent event) {
        mPid = event.pid;
    }

    public void onEventBackgroundThread(ApplicationStateChangedEvent event) {
        Timber.v("%d - %s", event.time, event.state.toString());
    }

    public void onEventBackgroundThread(MemoryInfoEvent event) {
        Timber.v("%d - %s", event.time, event.memoryInfo.toString());
        sendMessage(new MemoryInfoMessage(event.memoryInfo, event.time));
    }

    public void onEventBackgroundThread(CpuInfoEvent event) {
        Timber.v("%d - %s", event.time, event.cpuInfo.toString());
        sendMessage(new CpuInfoMessage(event.cpuInfo, event.time));
    }

    @DebugLog
    public void sendMessage(Message message) {
        try {
            if (mConnection != null && mConnection.isConnected()) {
                mConnection.sendMessage(message);
            }
        } catch (IOException e) {
            Timber.w(e, "");
            if (!mConnection.isConnected()) {
                startConnection(mHostname, mPort);
            }
        }
    }

    @DebugLog
    Notification buildNotification(ApplicationInfo appInfo) {

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


}
