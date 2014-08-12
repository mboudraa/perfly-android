package com.samantha.app.core.sys;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

public class Device {

    public final String imei;
    public final String brand;
    public final String model;
    public final String manufacturer;
    public final String versionName;
    public final Point dimension;

    private Device(String imei, String brand, String model, String manufacturer, String versionName, Point dimension) {
        this.imei = imei;
        this.brand = brand;
        this.model = model;
        this.manufacturer = manufacturer;
        this.versionName = versionName;
        this.dimension = dimension;
    }

    public static Device getInformations(Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final String imei = telephonyManager.getDeviceId();
        Point size = new Point();

        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            size.x = display.getWidth();
            size.y = display.getHeight();
        } else {
            display.getSize(size);
        }

        return new Device(telephonyManager.getDeviceId(),
                          Build.BRAND,
                          Build.MODEL,
                          Build.MANUFACTURER,
                          Build.VERSION.RELEASE,
                          size);

    }

}
