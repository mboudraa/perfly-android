package com.samantha.app.core;

import com.google.gson.annotations.Expose;

public class CpuInfo {

    @Expose
    public final int cpuTotal;
    @Expose
    public final int cpuUser;
    @Expose
    public final int cpuKernel;

    public CpuInfo(int cpuTotal, int cpuUser, int cpuKernel) {
        this.cpuTotal = cpuTotal;
        this.cpuUser = cpuUser;
        this.cpuKernel = cpuKernel;
    }


    @Override
    public String toString() {
        return String.format("CPU Total: %d%%, User: %d%%, Kernel: %d%%",
                             cpuTotal, cpuUser, cpuKernel);

    }


}
