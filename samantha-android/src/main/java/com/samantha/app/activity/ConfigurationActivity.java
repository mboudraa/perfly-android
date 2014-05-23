package com.samantha.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.samantha.app.R;


public class ConfigurationActivity extends BaseActivity {


    public static final String PREF_SERVER_KEY = "PREF_SERVER_KEY";
    public static final String PREF_NAME = "SAM_CONFIG_PREF";

    @InjectView(R.id.conf_server_edittext)
    EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        ButterKnife.inject(this);

        mEditText.setText(getConfiguration());

    }

    @OnClick(R.id.conf_scan_imageview)
    public void onScanClicked(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            mEditText.setText(scanResult.getContents());
        }
    }

    private void saveToPreferences(String serverUrl) {

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

    public String getConfiguration() {
        String serverConfiguration = getSharedPreferences(ConfigurationActivity.PREF_NAME, MODE_PRIVATE)
                .getString(ConfigurationActivity.PREF_SERVER_KEY, "");
        return serverConfiguration;
    }
}
