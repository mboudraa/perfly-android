package com.samantha.app.core;

public class CpuInfo {

    public final float cpuTotal;
    public final float cpuUser;
    public final float cpuKernel;

    public CpuInfo(float cpuTotal, float cpuUser, float cpuKernel) {
        this.cpuTotal = cpuTotal;
        this.cpuUser = cpuUser;
        this.cpuKernel = cpuKernel;
    }


    @Override
    public String toString() {
        return String.format("CPU Total: %f %%, User: %f %%, Kernel: %f %%",
                             cpuTotal, cpuUser, cpuKernel);

    }


}
