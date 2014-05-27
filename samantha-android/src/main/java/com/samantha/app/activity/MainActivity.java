package com.samantha.app.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import com.samantha.app.service.MonitoringService;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
    private static final String EXTRA_STOP_MONITORING = "EXTRA_STOP_MONITORING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        boolean isMonitoringServiceRunning = isMonitoringServiceRunning();
        String packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        boolean stopMonitoringIfExists = Boolean.parseBoolean(getIntent().getStringExtra(EXTRA_STOP_MONITORING));

        if (TextUtils.isEmpty(packageName)) {
            Intent intent = new Intent(this, ConfigurationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        try {

            if (stopMonitoringIfExists && isMonitoringServiceRunning) {
                stopMonitoringService();
                isMonitoringServiceRunning = false;
            }

            if (!isMonitoringServiceRunning) {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(packageName, 0);
                startMonitoringService(appInfo);
            } else {
                Timber.w("Monitoring service is already running");
            }

        } catch (PackageManager.NameNotFoundException e) {
            Timber.e("Application with package name '%s' not found. Monitoring could not be started",
                     packageName);
        } finally {
            finish();
        }

    }

    private boolean isMonitoringServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MonitoringService.class.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startMonitoringService(ApplicationInfo applicationInfo) {
        Intent serviceIntent = new Intent(MainActivity.this, MonitoringService.class);

        SharedPreferences prefs =  getSharedPreferences(ConfigurationActivity.PREF_NAME, MODE_PRIVATE);

        String hostname = prefs.getString(ConfigurationActivity.PREF_SERVER_KEY, "localhost");

        serviceIntent.putExtra(MonitoringService.EXTRA_HOSTNAME, hostname);
        serviceIntent.putExtra(MonitoringService.EXTRA_PORT, 80);

        startService(serviceIntent);

    }

    public void stopMonitoringService() {
        Intent serviceIntent = new Intent(MainActivity.this, MonitoringService.class);
        stopService(serviceIntent);
    }


}
