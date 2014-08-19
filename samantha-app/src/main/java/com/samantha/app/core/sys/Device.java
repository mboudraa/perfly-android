package com.samantha.app.core.sys;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import java.util.UUID;

public class Device {

    public final String id;
    public final String brand;
    public final String model;
    public final String manufacturer;
    public final String versionName;
    public final Point dimension;

    private Device(String id, String brand, String model, String manufacturer, String versionName, Point dimension) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.manufacturer = manufacturer;
        this.versionName = versionName;
        this.dimension = dimension;
    }

    public static Device getInformations(Context context) {


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();

        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            size.x = display.getWidth();
            size.y = display.getHeight();
        } else {
            display.getSize(size);
        }

        return new Device(
                getDeviceId(context),
                Build.BRAND,
                Build.MODEL,
                Build.MANUFACTURER,
                Build.VERSION.RELEASE,
                size);

    }

    private static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("SAMANTHA", Context.MODE_PRIVATE);
        final String key = "deviceId";
        String deviceId = prefs.getString(key, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString().replace("-", "");
            prefs.edit().putString(key, deviceId).apply();
        }
        return deviceId;
    }

}
