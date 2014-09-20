package com.perfly.android.core.sys;

public class MemoryInfo {

    public final int dalvikLimit;
    public final int appTotal;
    public final int appDalvik;
    public final int appNative;

    public final int pss;
    public final int privateDirty;

    public MemoryInfo(int dalvikLimit, int appTotal, int appDalvik, int pss, int privateDirty) {
        this.dalvikLimit = dalvikLimit;
        this.appTotal = appTotal;
        this.appDalvik = appDalvik;
        this.appNative = appTotal - appDalvik;
        this.pss = pss;
        this.privateDirty = privateDirty;

    }


    @Override
    public String toString() {
        return String.format("Dalvik limit: %dkb, App Total: %dkb, App Dalvik: %dkb, App Native: %dkb",
                             dalvikLimit, appTotal, appDalvik, appNative);

    }


}
