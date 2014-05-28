package com.samantha.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.samantha.app.service.MonitoringService;
import com.samantha.app.R;

public class MonitoringActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
    }

    public void onStopMonitoringClicked(View v) {
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        stopService(serviceIntent);

        finish();
    }

}
