package com.irefire.android.imdriving;

import android.view.*;
import android.widget.Button;
import android.widget.ShareActionProvider;
import com.irefire.android.imdriving.engine.Engine;
import com.irefire.android.imdriving.engine.SystemEngine;
import com.irefire.android.imdriving.event.DummyEvent;
import com.irefire.android.imdriving.service.NotificationProcessor;
import com.irefire.android.imdriving.utils.AppSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;

import com.irefire.android.imdriving.utils.Constants;
import com.irefire.android.imdriving.utils.Root;

import java.lang.reflect.Field;

public class MainActivity extends Activity implements View.OnClickListener{

	private static final int TEST_NOTIFICATION_ID = 0x612;

	private Handler mHandler = new Handler();
    private ShareActionProvider mShareActionProvider = null;
    private Button startButton = null;
    private AppSettings mAppSettings = null;
    private Engine mEngine = null;

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

	private BroadcastReceiver mNotificationCheckReceiver = new NotificationEnableReceiver();

	private NotificationManager mNotificationManager = null;

	private static final Logger l = LoggerFactory.getLogger(MainActivity.class.getSimpleName());
	
	private Dialog mNotificationServiceDialog = null;
    private boolean isNotificationServiceDialogShowing = false;

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
			return true;
		}else if(id == R.id.action_filter_apps) {
            return true;
        }else if(id == R.id.action_help_to_translate) {
            return true;
        }else if(id == R.id.action_subscribe) {
            return true;
        }
		return super.onOptionsItemSelected(item);
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(Constants.NOTIFICATION_ENABLED);
		this.registerReceiver(mNotificationCheckReceiver, filter);

        if(!isNotificationServiceDialogShowing) {
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
            if(menuKeyField != null) {
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

	private final class NotificationEnableReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.NOTIFICATION_ENABLED.equals(intent.getAction())) {
				// if we receive notification enabled broadcast, we will
				// re move the mShowEnableNotificationDialog.
				mHandler.removeCallbacks(mShowEnableNotificationDialog);
				
				if(mNotificationServiceDialog != null && mNotificationServiceDialog.isShowing()) {
					mNotificationServiceDialog.dismiss();
				}
				mNotificationServiceDialog = null;
                isNotificationServiceDialogShowing = false;
				l.debug("remove mShowEnableNotificationDialog");
			}
		}

	}

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_start_stop) {
            if(mAppSettings.getServiceStarted()) {
                ((Button)v).setText(R.string.start_driving);
                new Thread() {
                    public void run() {
                        mEngine.speak(getString(R.string.bye_bye_message));
                    }
                }.start();
                mAppSettings.setServiceStarted(false);
                NotificationProcessor.getInstance().stop();
            }else {
                ((Button)v).setText(R.string.stop_driving);
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
}
