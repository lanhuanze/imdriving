package com.irefire.android.imdriving;

import android.app.*;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.ShareActionProvider;
import com.irefire.android.imdriving.engine.Engine;
import com.irefire.android.imdriving.engine.SystemEngine;
import com.irefire.android.imdriving.parse.UpdateParseService;
import com.irefire.android.imdriving.service.NotificationProcessor;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Constants;
import com.irefire.android.imdriving.utils.Root;
import com.irefire.android.imdriving.utils.TimerRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final Logger l = LoggerFactory.getLogger(MainActivity.class.getSimpleName());
    private Handler mHandler = new Handler();
    private ShareActionProvider mShareActionProvider = null;
    private Button startButton = null;
    private AppSettings mAppSettings = null;
    private Engine mEngine = null;

    private NotificationManager mNotificationManager = null;
    private Dialog mNotificationServiceDialog = null;
    private boolean isNotificationServiceDialogShowing = false;

    private Dialog mBuyProVersionDialog = null;
    private boolean isBuyProVersionDialogShow = false;

    private void showEnabledNotificationDialog() {
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
    }

    private void showBuyProVersionDialog() {
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        builder.setMessage(R.string.try_version_notify);

        builder.setTitle(R.string.support_us);
        builder.setCancelable(false);

        builder.setPositiveButton(android.R.string.ok,
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBuyProVersionDialog = null;
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.irefire.android.imdriving"));
                        //i.addFlag(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                });

        builder.setNegativeButton(android.R.string.cancel,
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBuyProVersionDialog = null;
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                });

        mBuyProVersionDialog = builder.create();
        mBuyProVersionDialog.show();
    }

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

        l.debug("AccountInfoId:" + mAppSettings.getAccountInfoId());
        if (TextUtils.isEmpty(mAppSettings.getAccountInfoId())) {
            Intent intent = new Intent();
            intent.setAction(UpdateParseService.ACTION_QUERY_ACCOUNT_INFO);
            intent.setClass(this, UpdateParseService.class);
            startService(intent);
        }

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
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content, getPackageName()));
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
            Intent intent = new Intent();
            intent.setClass(this, TranslateActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_filter_app) {
            Intent intent = new Intent();
            intent.setClass(this, FilterAppActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String notificationsPackages = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (TextUtils.isEmpty(notificationsPackages) || !notificationsPackages.contains(getPackageName())) {
            if (!isNotificationServiceDialogShowing) {
                showEnabledNotificationDialog();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_start_stop) {
            TimerRecorder tr = TimerRecorder.getInstance();
            if (mAppSettings.getServiceStarted()) {
                ((Button) v).setText(R.string.start_driving);
                ((Button) v).setBackground(getResources().getDrawable(R.drawable.round_button_start));
                new Thread() {
                    public void run() {
                        mEngine.speak(getString(R.string.bye_bye_message));
                    }
                }.start();
                mAppSettings.setServiceStarted(false);
                NotificationProcessor.getInstance().stop();
                tr.stop();
            } else {
                l.debug("First use time:" + new Date(mAppSettings.getFirstUseTime()));
                l.debug("Current time:" + new Date(System.currentTimeMillis()));
                if(mAppSettings.getFirstUseTime() + Constants.ONE_WEEK < System.currentTimeMillis()) {
                    // 要查检是不是使用超过一周了。
                    // 然后再查检是不是过了一天的量了
                    if(tr.readQuota()) {
                        showBuyProVersionDialog();
                        return ;
                    }
                }
                ((Button) v).setText(R.string.stop_driving);
                ((Button) v).setBackground(getResources().getDrawable(R.drawable.round_button));
                new Thread() {
                    public void run() {
                        mEngine.speak(getString(R.string.welcome_message));
                    }
                }.start();
                mAppSettings.setServiceStarted(true);
                NotificationProcessor.getInstance().start();
                tr.start();
            }
        }else if(id == R.id.button_download_pro) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.irefire.android.imdriving"));
            //i.addFlag(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        private View.OnClickListener mListener = null;
        private Button mButton = null;
        private Button mDownloadButton = null;

        public PlaceholderFragment(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            mButton = (Button)rootView.findViewById(R.id.button_start_stop);
            mDownloadButton = (Button)rootView.findViewById(R.id.button_download_pro);
            mButton.setOnClickListener(mListener);
            mDownloadButton.setOnClickListener(mListener);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            this.getActivity().getActionBar().setTitle(R.string.app_name);
            if (!mAppSettings.getServiceStarted()) {
                mButton.setText(R.string.start_driving);
                mButton.setBackground(getResources().getDrawable(R.drawable.round_button_start));
            }else {
                mButton.setText(R.string.stop_driving);
                mButton.setBackground(getResources().getDrawable(R.drawable.round_button));
            }
        }
    }
}
