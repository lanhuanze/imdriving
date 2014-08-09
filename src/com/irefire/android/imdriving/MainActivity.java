package com.irefire.android.imdriving;

import android.accounts.Account;
import android.app.*;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.ShareActionProvider;
import com.google.gson.Gson;
import com.irefire.android.imdriving.engine.Engine;
import com.irefire.android.imdriving.engine.SystemEngine;
import com.irefire.android.imdriving.parse.AccountInfo;
import com.irefire.android.imdriving.service.NotificationProcessor;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Constants;
import com.irefire.android.imdriving.utils.Root;
import com.irefire.android.imdriving.utils.Systems;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int TEST_NOTIFICATION_ID = 0x612;
    private static final Logger l = LoggerFactory.getLogger(MainActivity.class.getSimpleName());
    private Handler mHandler = new Handler();
    private ShareActionProvider mShareActionProvider = null;
    private Button startButton = null;
    private AppSettings mAppSettings = null;
    private Engine mEngine = null;
    private BroadcastReceiver mNotificationCheckReceiver = new NotificationEnableReceiver();

    private NotificationManager mNotificationManager = null;
    private Dialog mNotificationServiceDialog = null;
    private boolean isNotificationServiceDialogShowing = false;
    private Runnable mShowEnableNotificationDialog = new Runnable() {
        public void run() {
            AlertDialog.Builder builder = new Builder(MainActivity.this);
            builder.setMessage(R.string.enable_notification_service_content);

            builder.setTitle(R.string.enable_notification_service_title);
            builder.setCancelable(false);

            builder.setPositiveButton(android.R.string.ok,
                    new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mNotificationServiceDialog = null;
                            startActivity(new Intent(
                                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        }
                    });

            builder.setNegativeButton(android.R.string.cancel,
                    new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mNotificationServiceDialog = null;
                            MainActivity.this.finish();
                            System.exit(0);
                        }
                    });

            mNotificationServiceDialog = builder.create();
            mNotificationServiceDialog.show();
            isNotificationServiceDialogShowing = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getOverflowMenu();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment(this)).commit();
        }

        boolean rooted = Root.isRooted();
        l.debug("rooted:" + rooted);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // test we will start our service automatically.
        //NotificationProcessor.getInstance().start();

        setVolumeControlStream(AudioManager.STREAM_MUSIC); // So that the 'Media Volume' applies to this activity

        mAppSettings = AppSettings.getInstance();
        mEngine = SystemEngine.getInstance();

        //Set<String> accountTypes = Systems.getAccountTypes(this);
        List<Account> accounts = Systems.getAccountsByType(this, null);
        if(accounts == null || accounts.size() < 1) {
            l.warn("We need Google account to TTS.");
            return;
        }
        String emailAddress = "";
        for(Account a: accounts) {
            if(a.type.equals("com.google")) {
                emailAddress = a.name;
                break;
            }
        }
        String deviceId = Systems.getUniqueIdentifier(this);
        l.debug("emailAddress:" + emailAddress);
        l.debug("deviceId:" + deviceId);
        final AccountInfo accountInfo = new AccountInfo(emailAddress, deviceId, accounts);
        new Thread() {
            public void run() {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpUriRequest get = accountInfo.getHttpGetWithQuery();
                try {
                    HttpResponse resp = client.execute(get);
                    l.debug("Get response status code:" + resp.getStatusLine().getStatusCode());
                    if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                       String body =  EntityUtils.toString(resp.getEntity());
                        l.debug("Get response body:" + body);
                        try {
                            JSONObject result = new JSONObject(body);
                            JSONArray arrays = result.getJSONArray("results");
                            if(arrays == null || arrays.length() < 1) {
                                HttpPost post = accountInfo.getHttpPost();

                                try {
                                    resp = client.execute(post);
                                    l.debug("Post response status code:" + resp.getStatusLine().getStatusCode());
                                    if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                                        body =  EntityUtils.toString(resp.getEntity());
                                        l.debug("Post response body:" + body);
                                        JSONObject created = new JSONObject(body);
                                        mAppSettings.setAccountInfoId(created.optString("objectId"));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                JSONObject obj = arrays.getJSONObject(0);
                                String accountInfoId = obj.optString("objectId");
                                if(!TextUtils.isEmpty(accountInfoId)) {
                                    mAppSettings.setAccountInfoId(accountInfoId);
                                }
                                AccountInfo info = new Gson().fromJson(obj.toString(), AccountInfo.class);
                                l.debug("Retrieve account info from net:" + info.toString());
                                mAppSettings.setFirstUseTime(info.getFistUsedTime());
                                mAppSettings.setSubscriptionStatus(info.getSubscriptionStatus());
                                mAppSettings.setSubscriptionType(info.getSubscriptionType());
                            }
                        }catch(JSONException e) {
                            l.error("JSON error:" + e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the ActionProvider
        mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share).getActionProvider();
        // Initialize the share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content));
        mShareActionProvider.setShareIntent(intent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_help_to_translate) {
            return true;
        } else if (id == R.id.action_filter_app) {
            Intent intent = new Intent();
            intent.setClass(this, FilterAppActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_subscribe) {
            Intent intent = new Intent();
            intent.setClass(this, IAPActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Constants.NOTIFICATION_ENABLED);
        this.registerReceiver(mNotificationCheckReceiver, filter);

        if (!isNotificationServiceDialogShowing) {
            // We will delay 200 to show the dialog in case we had enabled it.
            mHandler.postDelayed(mShowEnableNotificationDialog, 200);

            // then we generate a notification, if the Notificationlistener received
            // it.
            // He will send a broadcast to {NotificationEnableReciever};
            generateCheckNotification();
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    cleanCheckNotification();
                }
            }, 50); // we will clean the notification in 50 ms.
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mNotificationCheckReceiver);
    }

    private void generateCheckNotification() {
        Notification n = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.test_notification_title))
                .setContentText(getString(R.string.test_notification_content))
                .build();
        mNotificationManager.notify(TEST_NOTIFICATION_ID, n);
    }

    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanCheckNotification() {
        mNotificationManager.cancel(TEST_NOTIFICATION_ID);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_start_stop) {
            if (mAppSettings.getServiceStarted()) {
                ((Button) v).setText(R.string.start_driving);
                new Thread() {
                    public void run() {
                        mEngine.speak(getString(R.string.bye_bye_message));
                    }
                }.start();
                mAppSettings.setServiceStarted(false);
                NotificationProcessor.getInstance().stop();
            } else {
                ((Button) v).setText(R.string.stop_driving);
                new Thread() {
                    public void run() {
                        mEngine.speak(getString(R.string.welcome_message));
                    }
                }.start();
                mAppSettings.setServiceStarted(true);
                NotificationProcessor.getInstance().start();
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private View.OnClickListener mListener = null;

        public PlaceholderFragment(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            rootView.findViewById(R.id.button_start_stop).setOnClickListener(mListener);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            this.getActivity().getActionBar().setTitle(R.string.app_name);
        }
    }

    public static class AppSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.addPreferencesFromResource(R.xml.app_settings);
        }

        @Override
        public void onResume() {
            super.onResume();
            this.getActivity().getActionBar().setTitle(R.string.app_settings);
        }
    }

    private final class NotificationEnableReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.NOTIFICATION_ENABLED.equals(intent.getAction())) {
                // if we receive notification enabled broadcast, we will
                // re move the mShowEnableNotificationDialog.
                mHandler.removeCallbacks(mShowEnableNotificationDialog);

                if (mNotificationServiceDialog != null && mNotificationServiceDialog.isShowing()) {
                    mNotificationServiceDialog.dismiss();
                }
                mNotificationServiceDialog = null;
                isNotificationServiceDialogShowing = false;
                l.debug("remove mShowEnableNotificationDialog");
            }
        }

    }
}
