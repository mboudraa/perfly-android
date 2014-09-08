package com.samantha.app.core.sys;

public class MemoryInfo {

    public final int dalvikLimit;
    public final int appTotal;
    public final int appDalvik;
    public final int appNative;

    public final long allocatedHeapSize;
    public final long heapSize;
    public final long heapFreeSize;

    public MemoryInfo(int dalvikLimit, int appTotal, int appDalvik, long allocatedHeapSize, long heapFreeSize) {
        this.dalvikLimit = dalvikLimit;
        this.appTotal = appTotal;
        this.appDalvik = appDalvik;
        this.appNative = appTotal - appDalvik;
        this.heapFreeSize = heapFreeSize;
        this.allocatedHeapSize = allocatedHeapSize;
        this.heapSize = heapFreeSize + allocatedHeapSize;

    }


    @Override
    public String toString() {
        return String.format("Dalvik limit: %dkb, App Total: %dkb, App Dalvik: %dkb, App Native: %dkb",
                             dalvikLimit, appTotal, appDalvik, appNative);

    }


}
