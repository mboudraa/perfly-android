package com.samantha.app.job;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.samantha.app.core.CpuInfo;
import com.samantha.app.event.CpuInfoEvent;
import de.greenrobot.event.EventBus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;

public class CpuInfoJob extends Job {


    private final ApplicationInfo mApplicationInfo;
    private final int mPid;


    public CpuInfoJob(Context context, int pid, ApplicationInfo appInfo) {
        super(new Params(1).setRequiresNetwork(false).setPersistent(false));
        mApplicationInfo = appInfo;
        mPid = pid;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        long time = new Date().getTime();
        long totalCpuUsageBefore = getTotalCpuUsage();
        long utimeBefore = getCpuUsageByPid(mPid)[0];
        long stimeBefore = getCpuUsageByPid(mPid)[1];

        Thread.sleep(500);

        long totalCpuUsageAfter = getTotalCpuUsage();
        long utimeAfter = getCpuUsageByPid(mPid)[0];
        long stimeAfter = getCpuUsageByPid(mPid)[1];


        int userUsage = (int) (100 * (utimeAfter - utimeBefore) / (totalCpuUsageAfter - totalCpuUsageBefore));
        int kernelUsage = (int) (100 * (stimeAfter - stimeBefore) / (totalCpuUsageAfter - totalCpuUsageBefore));
        int total = userUsage + kernelUsage;

        EventBus.getDefault()
                .post(new CpuInfoEvent(mApplicationInfo, new CpuInfo(total, userUsage, kernelUsage), time));
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
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
