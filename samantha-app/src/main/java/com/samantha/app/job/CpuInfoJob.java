package com.samantha.app.job;

import android.content.Context;
import android.text.TextUtils;
import com.samantha.app.core.CpuInfo;
import com.samantha.app.event.CpuInfoEvent;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

import java.io.BufferedReader;
import java.io.FileReader;

public class CpuInfoJob extends MonitoringJob {


    private final int mPid;

    public CpuInfoJob(Context context, int pid, long time) {
        super(context, time);
        mPid = pid;
    }


    @Override
    public void onRun() throws Throwable {
        int userUsage = 0;
        int kernelUsage = 0;
        int total = 0;

        if (mPid > 0) {
            long totalCpuUsageBefore = getTotalCpuUsage();
            long utimeBefore = getCpuUsageByPid(mPid)[0];
            long stimeBefore = getCpuUsageByPid(mPid)[1];

            Thread.sleep(500);

            long totalCpuUsageAfter = getTotalCpuUsage();
            long utimeAfter = getCpuUsageByPid(mPid)[0];
            long stimeAfter = getCpuUsageByPid(mPid)[1];


            userUsage = (int) (100 * (utimeAfter - utimeBefore) / (totalCpuUsageAfter - totalCpuUsageBefore));
            kernelUsage = (int) (100 * (stimeAfter - stimeBefore) / (totalCpuUsageAfter - totalCpuUsageBefore));
            total = userUsage + kernelUsage;
        }

        EventBus.getDefault()
                .post(new CpuInfoEvent(new CpuInfo(total, userUsage, kernelUsage), getTime()));
    }


    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Timber.w(throwable, "");
        return false;
    }


    private long[] getCpuUsageByPid(int pid) throws Throwable {
        String line = new BufferedReader(new FileReader(String.format("/proc/%d/stat", mPid))).readLine().trim();
        String[] values = line.split(" ");
        long utime = Long.parseLong(values[13]);
        long stime = Long.parseLong(values[14]);

        return new long[]{utime, stime};
    }

    private long getTotalCpuUsage() throws Throwable {

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
