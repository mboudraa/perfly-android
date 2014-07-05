package com.samantha.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.samantha.app.service.MonitoringService;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
    private static final String EXTRA_STOP_MONITORING = "EXTRA_STOP_MONITORING";
    private String mPackageName;
    private boolean mStopMonitoringIfRunning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPackageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        mStopMonitoringIfRunning = Boolean.parseBoolean(getIntent().getStringExtra(EXTRA_STOP_MONITORING));


        if (TextUtils.isEmpty(mPackageName)) {
            Intent intent = new Intent(this, ConfigurationActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onServiceConnected(MonitoringService monitoringService) {
        if (!isFinishing()) {
            monitoringService.startMonitoring(mPackageName);
            finish();
        }
    }

}
