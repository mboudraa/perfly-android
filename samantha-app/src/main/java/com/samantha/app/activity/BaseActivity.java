package com.samantha.app.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import com.samantha.app.event.OnConnectionEvent;
import com.samantha.app.event.OnFinishSendingApplicationsEvent;
import com.samantha.app.event.OnProgressSendingApplicationsEvent;
import com.samantha.app.event.OnStartSendingApplicationsEvent;
import com.samantha.app.service.MonitoringService;
import de.greenrobot.event.EventBus;
import icepick.Icepick;

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


    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }


    @Override
    protected void onStart() {
        super.onStart();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIndeterminate(false);


        EventBus.getDefault().register(this);

        SharedPreferences prefs = getSharedPreferences(ConfigurationActivity.PREF_NAME, MODE_PRIVATE);
        String hostname = prefs.getString(ConfigurationActivity.PREF_SERVER_KEY, "");

        Intent serviceIntent = new Intent(this, MonitoringService.class);
        serviceIntent.putExtra(MonitoringService.EXTRA_HOSTNAME, hostname);
        serviceIntent.putExtra(MonitoringService.EXTRA_PORT, 8888);

        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        startService(serviceIntent);
    }


    @Override
    protected void onPause() {
        mProgressDialog.dismiss();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mMonitoringService != null) {
            unbindService(mConnection);
        }
        EventBus.getDefault().unregister(this);
        super.onStop();
    }



    public void onEventMainThread(OnConnectionEvent e) {
        mProgressDialog.dismiss();
        Toast.makeText(this, "Connection with server is lost", Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(OnStartSendingApplicationsEvent e) {
        mProgressDialog.setMax(e.total);
        mProgressDialog.setMessage("Synchronizing Applications");
        mProgressDialog.show();
    }

    public void onEventMainThread(OnProgressSendingApplicationsEvent e) {
        mProgressDialog.setMessage(String.format("Synchronizing %s", e.application.label));
        mProgressDialog.setProgress(e.progress);
    }

    public void onEventMainThread(OnFinishSendingApplicationsEvent e) {
        mProgressDialog.dismiss();
    }

    protected void onServiceConnected(MonitoringService monitoringService) {
    }

    protected void onServiceDisconnected() {
    }
}
