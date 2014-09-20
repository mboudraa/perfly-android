package com.perfly.android.activity;

import android.os.Bundle;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.perfly.android.service.MonitoringService;
import com.perfly.android.R;

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
