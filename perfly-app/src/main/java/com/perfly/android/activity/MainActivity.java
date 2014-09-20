/*
 * Copyright (c) 2014 Mounir Boudraa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perfly.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.crashlytics.android.Crashlytics;
import com.perfly.android.service.MonitoringService;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME";
    private static final String EXTRA_STOP_MONITORING = "EXTRA_STOP_MONITORING";
    private String mPackageName;
    private boolean mStopMonitoringIfRunning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);

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
