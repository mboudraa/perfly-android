package com.samantha.app.service;

import android.app.*;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.core.ConditionTimeoutException;
import com.path.android.jobqueue.JobManager;
import com.samantha.app.R;
import com.samantha.app.SamApplication;
import com.samantha.app.activity.MonitoringActivity;
import com.samantha.app.core.CpuInfoMessage;
import com.samantha.app.core.MemoryInfoMessage;
import com.samantha.app.event.*;
import com.samantha.app.job.CpuInfoJob;
import com.samantha.app.job.MemoryInfoJob;
import com.samantha.app.net.Connection;
import com.samantha.app.net.Message;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class MonitoringService extends Service {


    public static final String EXTRA_APPLICATION_INFO = "APPLICATION_INFO_EXTRA";
    public static final String EXTRA_TIMEOUT = "TIMEOUT_EXTRA";
    public static final String EXTRA_HOSTNAME = "EXTRA_HOSTNAME";
    public static final String EXTRA_PORT = "EXTRA_PORT";

    private static final int PID_NONE = -1;
    private static final int DEFAULT_TIMEOUT = 10;
    private static final int NOTIFICATION_ID = 0x01;
    private static final int DEFAULT_PORT = 80;

    Connection mConnection = new Connection();

    ActivityManager mActivityManager;
    ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture mMonitoringHandler;
    JobManager mJobManager = SamApplication.getInstance().getJobManager();
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mNotificationBuilder;
    OrientationChangedEventListener mOrientationChangedEventReceiver;

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
        mOrientationChangedEventReceiver = new OrientationChangedEventListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        ApplicationInfo appInfo = intent.getParcelableExtra(EXTRA_APPLICATION_INFO);
        int timeOut = intent.getIntExtra(EXTRA_TIMEOUT, DEFAULT_TIMEOUT);
        int port = intent.getIntExtra(EXTRA_PORT, DEFAULT_PORT);
        String hostname = intent.getStringExtra(EXTRA_HOSTNAME);

        try {
            mConnection.connect(hostname, DEFAULT_PORT);
        } catch (IOException e) {
            Toast.makeText(this, "Impossible to connect to server", Toast.LENGTH_LONG).show();
            Timber.e(e, "");
            stopSelf();
        }


        startForeground(NOTIFICATION_ID, buildNotification(appInfo));
        waitForPid(appInfo, timeOut, TimeUnit.SECONDS);

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        Timber.i("STOPPING MONITORING SERVICE");

        EventBus.getDefault().unregister(this);
        stopMonitoring();
        stopForeground(true);

        try {
            mConnection.close();
        } catch (IOException e) {
            Timber.e("", e);
        }

    }

    public void waitForPid(final ApplicationInfo appInfo, final int timeOut, final TimeUnit timeUnit) {
        Timber.i("WAITING FOR PID OF '%s'", getPackageManager().getApplicationLabel(appInfo));

        mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                Awaitility.setDefaultTimeout(timeOut, timeUnit);
                Awaitility.setDefaultPollDelay(100, TimeUnit.MILLISECONDS);
                try {
                    Awaitility.await().until(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            final int pid = getPid(appInfo);
                            if (pid != PID_NONE) {
                                Timber.i("PID FOUND -> %d", pid);
                                startMonitoring(pid, appInfo);
                                return true;
                            }

                            return false;
                        }
                    });
                } catch (ConditionTimeoutException e) {
                    Timber.e("Impossible to find pid for '%s'.", appInfo.packageName);
                    stopSelf();
                }

            }
        }, 0, TimeUnit.SECONDS);
    }

    public int getPid(ApplicationInfo appInfo) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = mActivityManager.getRunningAppProcesses();

        if (runningAppProcessInfos != null && !runningAppProcessInfos.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfos) {
                if (appInfo.processName.equals(processInfo.processName)) {
                    return processInfo.pid;
                }
            }
        }

        return PID_NONE;
    }

    public void startActivityToMonitor(String packageName) {
        Intent monitoredAppIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        monitoredAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(monitoredAppIntent);
    }

    public void startMonitoring(final int pid, final ApplicationInfo applicationInfo) {
        mOrientationChangedEventReceiver.enable();
        mMonitoringHandler = mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mJobManager.addJobInBackground(new MemoryInfoJob(MonitoringService.this, pid, applicationInfo));
                mJobManager.addJobInBackground(new CpuInfoJob(MonitoringService.this, pid, applicationInfo));
            }
        }, 0, 1, TimeUnit.SECONDS);

    }


    public void stopMonitoring() {
        mOrientationChangedEventReceiver.disable();
        mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (mMonitoringHandler != null) {
                    mMonitoringHandler.cancel(true);
                }
            }
        }, 0, TimeUnit.SECONDS);
    }

    public void onEventBackgroundThread(MemoryInfoEvent event) {
        Timber.v("%d - %s", event.time, event.memoryInfo.toString());
        sendMessage(new MemoryInfoMessage(event.memoryInfo, event.time));

    }

    public void onEventBackgroundThread(CpuInfoEvent event) {
        Timber.v("%d - %s", event.time, event.cpuInfo.toString());
        sendMessage(new CpuInfoMessage(event.cpuInfo, event.time));
    }

    public void onEventBackgroundThread(SystemEvent event) {
        Timber.v("%d - %s", event.time, event.action);
        //TODO PUSH TO SERVER
    }

    public void onEventBackgroundThread(OrientationChangedEvent event) {
        Timber.v("%d - %s", event.time,
                 event.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait");
        //TODO PUSH TO SERVER
    }


    private void sendMessage(Message message) {
        try {
            mConnection.sendMessage(message);
        } catch (IOException e) {
            Timber.w(e, "");
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
}
