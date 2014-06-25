package com.irefire.android.imdriving;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.irefire.android.imdriving.utils.Constants;
import com.irefire.android.imdriving.utils.Root;

public class MainActivity extends Activity {

	private static final int TEST_NOTIFICATION_ID = 0x612;

	private Handler mHandler = new Handler();

	private Runnable mShowEnableNotificationDialog = new Runnable() {
		public void run() {
			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setMessage(R.string.enable_notification_service_content);

			builder.setTitle(R.string.enable_notification_service_title);

			builder.setPositiveButton(android.R.string.ok,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							startActivity(new Intent(
									"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
						}
					});

			builder.setNegativeButton(android.R.string.cancel,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							MainActivity.this.finish();
						}
					});

			builder.create().show();
		}
	};

	private BroadcastReceiver mNotificationCheckReceiver = new NotificationEnableReciever();

	private NotificationManager mNotificationManager = null;

	private static final Logger l = LoggerFactory.getLogger(MainActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		boolean rooted = Root.isRooted();
		l.debug("rooted:" + rooted);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(Constants.NOTIFICATION_ENABLED);
		this.registerReceiver(mNotificationCheckReceiver, filter);

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

	private void cleanCheckNotification() {
		mNotificationManager.cancel(TEST_NOTIFICATION_ID);
	}

	private final class NotificationEnableReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.NOTIFICATION_ENABLED.equals(intent.getAction())) {
				// if we receive notification enabled broadcast, we will
				// re move the mShowEnableNotificationDialog.
				mHandler.removeCallbacks(mShowEnableNotificationDialog);
				l.debug("remove mShowEnableNotificationDialog");
			}
		}

	}
}
