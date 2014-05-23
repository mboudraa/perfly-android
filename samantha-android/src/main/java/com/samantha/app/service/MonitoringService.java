package com.samantha.app.service;

import android.app.*;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.core.ConditionTimeoutException;
import com.path.android.jobqueue.JobManager;
import com.samantha.app.R;
import com.samantha.app.SamApplication;
import com.samantha.app.activity.MonitoringActivity;
import com.samantha.app.event.CpuInfoEvent;
import com.samantha.app.event.MemoryInfoEvent;
import com.samantha.app.job.CpuInfoJob;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.util.List;
import java.util.concurrent.*;

public class MonitoringService extends Service {

    public static final String EXTRA_APPLICATION_INFO = "APPLICATION_INFO_EXTRA";
    public static final String EXTRA_TIMEOUT = "TIMEOUT_EXTRA";

    private static final int PID_NONE = -1;
    private static final int DEFAULT_TIMEOUT = 10;
    private static final int NOTIFICATION_ID = 0x01;

    ActivityManager mActivityManager;
    ScheduledExecutorService mScheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture mMonitoringHandler;
    JobManager mJobManager = SamApplication.getInstance().getJobManager();
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mNotificationBuilder;


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
        int timeOut = intent.getIntExtra(EXTRA_TIMEOUT, DEFAULT_TIMEOUT);

        startForeground(NOTIFICATION_ID, buildNotification(appInfo));
        waitForPid(appInfo, timeOut, TimeUnit.SECONDS);

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        Timber.i("STOPPING MONITORING SERVICE");
        stopMonitoring();
        stopForeground(true);
        EventBus.getDefault().unregister(this);
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

    public void startActivityToMonitor(ApplicationInfo appInfo) {
        Intent monitoredAppIntent = getPackageManager().getLaunchIntentForPackage(appInfo.packageName);
        monitoredAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(monitoredAppIntent);
    }

    public void startMonitoring(final int pid, final ApplicationInfo applicationInfo) {
        mMonitoringHandler = mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
//                mJobManager.addJob(new MemoryInfoJob(MonitoringService.this, pid, applicationInfo));
                mJobManager.addJob(new CpuInfoJob(MonitoringService.this, pid, applicationInfo));
            }
        }, 0, 1, TimeUnit.SECONDS);

    }


    public void stopMonitoring() {
        mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (mMonitoringHandler != null) {
                    mMonitoringHandler.cancel(true);
                }
            }
        }, 0, TimeUnit.SECONDS);
    }

    public void onEvent(MemoryInfoEvent event) {
        Timber.v(event.memoryInfo.toString());
        //TODO PUSH MEMORYINFO TO SERVER
    }

    public void onEvent(CpuInfoEvent event) {
        Timber.v(event.cpuInfo.toString());
        //TODO PUSH MEMORYINFO TO SERVER
    }


    private Notification buildNotification(ApplicationInfo appInfo) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Monitoring " + getPackageManager().getApplicationLabel(appInfo));


        Intent activityIntent = new Intent(this, MonitoringActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent,
                                                                Intent.FLAG_ACTIVITY_NEW_TASK);

        mNotificationBuilder
                .setContentTitle("Monitoring " + getPackageManager().getApplicationLabel(appInfo))
                .setSmallIcon(R.drawable.ic_launcher)
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent);


        return mNotificationBuilder.build();
    }
}
