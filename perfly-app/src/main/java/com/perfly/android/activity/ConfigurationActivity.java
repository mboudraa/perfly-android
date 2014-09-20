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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.path.android.jobqueue.JobManager;
import com.perfly.android.PerflyApplication;
import com.perfly.android.R;
import com.perfly.android.event.OnConnectionEvent;
import com.perfly.android.service.MonitoringService;
import de.greenrobot.event.EventBus;
import icepick.Icicle;


public class ConfigurationActivity extends BaseActivity {


    public static final String PREF_SERVER_KEY = "PREF_SERVER_KEY";
    public static final String PREF_NAME = "SAM_CONFIG_PREF";

    @InjectView(R.id.conf_server_edittext)
    EditText mEditText;

    @InjectView(R.id.conf_connect_button)
    Button mConnectButton;

    @InjectView(R.id.conf_status_textview)
    TextView mStatusTextView;

    @Icicle
    String mConfigurationServer;

    @Icicle
    boolean mConnected;

    @Icicle
    boolean mConnectingDialogOpen;

    JobManager mJobManager = PerflyApplication.getInstance().getJobManager();
    EventBus mEventBus = EventBus.getDefault();

    private MonitoringService mMonitoringService;
    private ProgressDialog mConnectingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        ButterKnife.inject(this);

        if (mConfigurationServer == null) {
            mConfigurationServer = getServerConfiguration();
        }

        mEditText.setText(mConfigurationServer);
        updateStatusTextView(mConnected);
    }

    @Override
    protected void onServiceConnected(MonitoringService monitoringService) {
        mMonitoringService = monitoringService;
        mConnectButton.setEnabled(true);
        updateStatusTextView(mMonitoringService.isConnectionOpen());

    }

    @Override
    protected void onServiceDisconnected() {
        mMonitoringService = null;
    }

    @OnClick(R.id.conf_scan_imageview)
    public void onScanClicked() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    @OnClick(R.id.conf_connect_button)
    public void onConnectClicked() {

        if (mConnectingDialog != null) {
            mConnectingDialog.dismiss();
        }
        mConnectingDialog = showConnectingDialog();

        final String newConfServer = mEditText.getText().toString();
        if (mConfigurationServer == null || !mConfigurationServer.equals(newConfServer)) {
            saveToPreferences(newConfServer);
        }

        mMonitoringService.openConnection(newConfServer);

    }

    public void onEventMainThread(OnConnectionEvent event) {
        mConnected = event.connected;
        updateStatusTextView(mConnected);

        if (mConnectingDialog != null) {
            mConnectingDialog.dismiss();
            mConnectingDialog = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            mEditText.setText(scanResult.getContents());
            onConnectClicked();
        }
    }

    private void saveToPreferences(String serverUrl) {
        mConfigurationServer = serverUrl;

        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(PREF_SERVER_KEY, serverUrl);

        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventBus.register(this);
    }


    @Override
    protected void onStop() {
        mEventBus.unregister(this);
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mConnectingDialogOpen) {
            mConnectingDialog = showConnectingDialog();
        }
    }

    @Override
    protected void onPause() {
        mConnectingDialogOpen = mConnectingDialog != null && mConnectingDialog.isShowing();
        dismissConnectingDialog();
        super.onPause();
    }

    public String getServerConfiguration() {
        String serverConfiguration = getSharedPreferences(ConfigurationActivity.PREF_NAME, MODE_PRIVATE)
                .getString(ConfigurationActivity.PREF_SERVER_KEY, "");
        return serverConfiguration;
    }


    private void updateStatusTextView(boolean connected) {
        if (connected) {
            mStatusTextView.setText(R.string.connected);
            mStatusTextView.setTextColor(getResources().getColor(R.color.connected));
        } else {
            mStatusTextView.setText(R.string.not_connected);
            mStatusTextView.setTextColor(getResources().getColor(R.color.not_connected));
        }
    }


    private ProgressDialog showConnectingDialog() {
        dismissConnectingDialog();
        return ProgressDialog.show(this, null, "Connecting", true, false);
    }

    private void dismissConnectingDialog() {
        if (mConnectingDialog != null) {
            mConnectingDialog.dismiss();
            mConnectingDialog = null;
        }
    }

}
