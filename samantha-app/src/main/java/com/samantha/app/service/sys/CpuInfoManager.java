package com.samantha.app.service.sys;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import com.samantha.app.core.sys.ApplicationStatus;
import com.samantha.app.core.sys.CpuInfo;
import com.samantha.app.event.ApplicationPidChangedEvent;
import com.samantha.app.event.CpuInfoEvent;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CpuInfoManager extends AbstractManager {

    ScheduledExecutorService mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture mScheduledFuture;
    EventBus mEventBus = EventBus.getDefault();
    private int mPid = ApplicationStatus.PID_NONE;

    CpuInfoManager(Context context, ApplicationInfo applicationInfo) {
        super(context, applicationInfo);
    }


    @Override
    public void start() {
        mEventBus.register(this);
        mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    long time = new Date().getTime();
                    CpuInfo cpuInfo = dump();
                    mEventBus.post(new CpuInfoEvent(cpuInfo, time));
                } catch (Exception e) {
                    Timber.e(e, "Dump CPU Info failed");
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        mScheduledExecutorService.shutdown();
        mScheduledFuture.cancel(true);
        mEventBus.unregister(this);
    }

    public void onEventBackgroundThread(ApplicationPidChangedEvent event) {
        mPid = event.pid;
    }

    private CpuInfo dump() throws Exception {
        int userUsage = 0;
        int kernelUsage = 0;
        int total = 0;

        if (mPid != ApplicationStatus.PID_NONE) {
            long totalCpuUsageBefore = getTotalCpuUsage();
            long utimeBefore = getCpuUsageByPid(mPid)[0];
            long stimeBefore = getCpuUsageByPid(mPid)[1];

            Thread.sleep(200);

            long totalCpuUsageAfter = getTotalCpuUsage();
            long utimeAfter = getCpuUsageByPid(mPid)[0];
            long stimeAfter = getCpuUsageByPid(mPid)[1];


            userUsage = (int) (100 * (utimeAfter - utimeBefore) / (totalCpuUsageAfter - totalCpuUsageBefore));
            kernelUsage = (int) (100 * (stimeAfter - stimeBefore) / (totalCpuUsageAfter - totalCpuUsageBefore));
            total = userUsage + kernelUsage;
        }

        return new CpuInfo(total, userUsage, kernelUsage);
    }

    private long[] getCpuUsageByPid(int pid) throws Exception {
        String line = new BufferedReader(new FileReader(String.format("/proc/%d/stat", mPid))).readLine().trim();
        String[] values = line.split(" ");
        long utime = Long.parseLong(values[13]);
        long stime = Long.parseLong(values[14]);

        return new long[]{utime, stime};
    }

    private long getTotalCpuUsage() throws Exception {

        String line = new BufferedReader(new FileReader("/proc/stat")).readLine().trim();

        String[] values = line.split(" ");

        long result = 0;
        for (int i = 1; i < values.length; i++) {
            if (!TextUtils.isEmpty(values[i])) {
                result += Long.parseLong(values[i]);
            }
        }
        return result;
    }
}
