package com.samantha.app.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.path.android.jobqueue.JobManager;
import com.samantha.app.R;
import com.samantha.app.SamApplication;
import com.samantha.app.core.api.ApplicationApi;
import com.samantha.app.event.ApplicationsInstalledEvent;
import com.samantha.app.job.ListInstalledApplicationsJob;
import de.greenrobot.event.EventBus;
import retrofit.ResponseCallback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.JacksonConverter;
import timber.log.Timber;


public class ConfigurationActivity extends BaseActivity implements TextWatcher {


    public static final String PREF_SERVER_KEY = "PREF_SERVER_KEY";
    public static final String PREF_NAME = "SAM_CONFIG_PREF";

    @InjectView(R.id.conf_server_edittext)
    EditText mEditText;

    @InjectView(R.id.conf_send_button)
    Button mSendButton;

    ProgressDialog mProgressDialog;

    String mConfigurationServer;

    RestAdapter mRestAdapter;

    JobManager mJobManager = SamApplication.getInstance().getJobManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        ButterKnife.inject(this);

        mConfigurationServer = getServerConfiguration();
        mRestAdapter = createRestAdapter(mConfigurationServer);

        mEditText.setText(mConfigurationServer);
        mEditText.addTextChangedListener(this);
        mSendButton.setEnabled(!TextUtils.isEmpty(mConfigurationServer));

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
    }


    @OnClick(R.id.conf_scan_imageview)
    public void onScanClicked() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    @OnClick(R.id.conf_send_button)
    public void onSendClicked() {
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading Applications");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();


        final String newConfServer = mEditText.getText().toString();
        if (mConfigurationServer == null || !mConfigurationServer.equals(newConfServer)) {
            saveToPreferences(newConfServer);
            mRestAdapter = createRestAdapter(newConfServer);
        }

        mJobManager.addJobInBackground(new ListInstalledApplicationsJob(this));
    }

    public void onEventMainThread(final ApplicationsInstalledEvent event) {
        final ApplicationApi appApi = mRestAdapter.create(ApplicationApi.class);
        final int maxCount = event.applications.size();

        mProgressDialog.dismiss();

        mProgressDialog.setMessage(String.format("Synchronizing App %d/%d", 0, maxCount));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(maxCount);
        mProgressDialog.show();

        appApi.postApplication(event.applications.get(0), new ResponseCallback() {

            int count = 0;

            @Override
            public void success(Response response) {
                count++;
                mProgressDialog.setProgress(count);

                if (count < maxCount) {
                    appApi.postApplication(event.applications.get(count), this);
                    mProgressDialog.setMessage(String.format("Synchronizing App %d/%d", count, maxCount));

                } else {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Timber.e(error, "");
                mProgressDialog.dismiss();
                Toast.makeText(ConfigurationActivity.this, "Impossible to synchronize apps with server",
                               Toast.LENGTH_LONG).show();
            }
        });
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


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!mSendButton.isEnabled() && !TextUtils.isEmpty(s)) {
            mSendButton.setEnabled(true);
        }
    }

    private RestAdapter createRestAdapter(String confServer) {
        return new RestAdapter.Builder()
                .setEndpoint("http://" + confServer + ":8080")
                .setConverter(new JacksonConverter())
                .build();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }


}
