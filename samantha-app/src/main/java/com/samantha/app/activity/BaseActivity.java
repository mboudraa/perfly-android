package com.samantha.app.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import com.samantha.app.service.MonitoringService;

public abstract class BaseActivity extends ActionBarActivity {

    private MonitoringService mMonitoringService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MonitoringService.Binder binder = (MonitoringService.Binder) service;
            mMonitoringService = binder.getService();
            BaseActivity.this.onServiceConnected(mMonitoringService);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mMonitoringService = null;
            BaseActivity.this.onServiceDisconnected();
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs =  getSharedPreferences(ConfigurationActivity.PREF_NAME, MODE_PRIVATE);
        String hostname = prefs.getString(ConfigurationActivity.PREF_SERVER_KEY, "");

        Intent serviceIntent = new Intent(this, MonitoringService.class);
        serviceIntent.putExtra(MonitoringService.EXTRA_HOSTNAME, hostname);
        serviceIntent.putExtra(MonitoringService.EXTRA_PORT, 8888);

        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        startService(serviceIntent);
    }

    @Override
    protected void onStop() {
        if (mMonitoringService != null) {
            unbindService(mConnection);
        }
        super.onStop();
    }


    protected void onServiceConnected(MonitoringService monitoringService){}

    protected void onServiceDisconnected(){}
}
