package com.samantha.app.core;

public class MemoryInfo {

    public final int dalvikLimit;
    public final int appTotal;
    public final int appDalvik;
    public final int appNative;

    public MemoryInfo(int dalvikLimit, int appTotal, int appDalvik) {
        this.dalvikLimit = dalvikLimit;
        this.appTotal = appTotal;
        this.appDalvik = appDalvik;
        this.appNative = appTotal - appDalvik;
    }


    @Override
    public String toString() {
        return String.format("Dalvik limit: %dkb, App Total: %dkb, App Dalvik: %dkb, App Native: %dkb",
                             dalvikLimit, appTotal, appDalvik, appNative);

    }


}
