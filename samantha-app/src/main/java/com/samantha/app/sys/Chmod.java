package com.samantha.app.sys;

import android.os.Build;

import java.io.File;
import java.io.IOException;

public abstract class Chmod {

    private static final Chmod sInstance;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            sInstance = new Java6Chmod();
        } else {
            sInstance = new Java5Chmod();
        }
    }

    static void chmodPlusR(File file) {
        sInstance.plusR(file);
    }

    static void chmodPlusRWX(File file) {
        sInstance.plusRWX(file);
    }

    protected abstract void plusR(File file);
    protected abstract void plusRWX(File file);

    private static class Java5Chmod extends Chmod {
        @Override protected void plusR(File file) {
            try {
                Runtime.getRuntime().exec(new String[] {"chmod", "644", file.getAbsolutePath()});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override protected void plusRWX(File file) {
            try {
                Runtime.getRuntime().exec(new String[] {"chmod", "777", file.getAbsolutePath()});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Java6Chmod extends Chmod {
        @Override protected void plusR(File file) {
            file.setReadable(true, false);
        }

        @Override protected void plusRWX(File file) {
            file.setReadable(true, false);
            file.setWritable(true, false);
            file.setExecutable(true, false);
        }
    }
}
