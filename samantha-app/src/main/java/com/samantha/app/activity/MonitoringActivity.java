package com.samantha.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.samantha.app.service.MonitoringService;
import com.samantha.app.R;

public class MonitoringActivity extends BaseActivity {


    MonitoringService mMonitoringService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.monitoringStopButton)
    public void onStopMonitoringClicked() {
        mMonitoringService.stopMonitoring();
        finish();
    }


    @Override
    protected void onServiceConnected(MonitoringService monitoringService) {
        mMonitoringService = monitoringService;
    }
}
