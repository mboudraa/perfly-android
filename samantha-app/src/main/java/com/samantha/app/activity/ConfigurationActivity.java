package com.samantha.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.path.android.jobqueue.JobManager;
import com.samantha.app.R;
import com.samantha.app.SamApplication;
import com.samantha.app.event.OnConnectionEvent;
import com.samantha.app.service.MonitoringService;
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

    JobManager mJobManager = SamApplication.getInstance().getJobManager();


    private MonitoringService mMonitoringService;

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

        final String newConfServer = mEditText.getText().toString();
        if (mConfigurationServer == null || !mConfigurationServer.equals(newConfServer)) {
            saveToPreferences(newConfServer);
        }

        if (!mMonitoringService.isConnectionOpen()) {
            mMonitoringService.openConnection();
        }


    }

    public void onEventMainThread(OnConnectionEvent event) {
        mConnected = event.connected;
        updateStatusTextView(mConnected);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            mEditText.setText(scanResult.getContents());
        }
    }

    private void saveToPreferences(String serverUrl) {
        mConfigurationServer = serverUrl;

        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(PREF_SERVER_KEY, serverUrl);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void onValidateSelected() {

        final String newServerUrl = mEditText.getText().toString();

        if (TextUtils.isEmpty(newServerUrl)) {
            mEditText.setError("invalide");
        } else {
            saveToPreferences(newServerUrl);
            finish();
        }
    }

    public void onCancelSelected() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_configuration_accept:
                onValidateSelected();
                return true;

            case R.id.menu_configuration_cancel:
                onCancelSelected();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_configuration, menu);
        return true;
    }

    public String getServerConfiguration() {
        String serverConfiguration = getSharedPreferences(ConfigurationActivity.PREF_NAME, MODE_PRIVATE)
                .getString(ConfigurationActivity.PREF_SERVER_KEY, "");
        return serverConfiguration;
    }


    private void updateStatusTextView(boolean connected) {
        if (connected) {
            mStatusTextView.setText(R.string.connected);
            mStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            mStatusTextView.setText(R.string.not_connected);
            mStatusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

}
