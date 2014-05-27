package com.samantha.app.core;

import com.google.gson.annotations.Expose;

public class MemoryInfo {

    @Expose
    public final int dalvikLimit;
    @Expose
    public final int appTotal;
    @Expose
    public final int appDalvik;
    @Expose
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
