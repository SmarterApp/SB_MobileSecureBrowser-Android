/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.air.mobilebrowser.softkeyboard.KeyboardUtil;

/**
 * This Service runs in the background while the Secure Browser is active. It
 * performs a check at a scheduled interval to determine the secure status of
 * the application. If it is detected that the Secure Browser is not the active
 * foreground activity. It automatically restarts the browser, bringing it to the front
 * of the activity stack. 
 * If a security threat is otherwise detected by the service, a local broadcast is
 * sent to alert other components of the application to handle.
 */
public class ActivityWatchService extends Service 
{
	private static final ClipData NEW_PLAIN_TEXT = ClipData.newPlainText("", "");
	private static final String TAG = "ActivityWatchService";
	private static final int SERVICE_ID = 1337;
	
	/** List of approved/whitelisted applications */
	protected ArrayList<String> mApplicationWhitelist;
	/** Last/Current to Service keyboard selected by user */
	protected String mLastKeyboard;
	/** Timer for managing application check */
	protected Timer mTimer;
	/** Ref to AudioManager for checking mic mute status */
	private AudioManager mAudioManager;
	/** Last/current mic mute status */
	private boolean mMicMute;
	private ClipboardManager mClipboardManager;
	private Set<String> mAppBlacklist = new HashSet<String>();

	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}

	@Override
	public void onCreate() 
	{
		super.onCreate();

		//Create an application whitelist, by default, we include android :-)
		mApplicationWhitelist = new ArrayList<String>();
		mApplicationWhitelist.add("android");
		
		String[] blackApps = super.getResources().getStringArray(R.array.blacklist_apps);
		for (String bapp : blackApps) {
			mAppBlacklist.add(bapp);
		}
		
		// For Android 5, also add com.google.android.music:main to the block list.
		// We need to detect if music is being played while the browser is running.
		// This process is shown on Android 5 only when the music is being played, while for
		// older Androids, the process list does not reflect whether music is being played or not
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
			String musicAppProcess = "com.google.android.music:main";
			mAppBlacklist.add(musicAppProcess);
		}
		
		//Save current keyboard
		mLastKeyboard = KeyboardUtil.getKeyboardPackage(super.getContentResolver());
		
		mClipboardManager = (ClipboardManager)super.getSystemService(Context.CLIPBOARD_SERVICE);

		//Save current keyboard
		mLastKeyboard = KeyboardUtil.getKeyboardPackage(super.getContentResolver());
		
		//Get a reference to the AudioManager to check mic mute status
		mAudioManager = (AudioManager)super.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		mMicMute = mAudioManager.isMicrophoneMute();
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setWhen(System.currentTimeMillis());
		builder.setTicker("Secure Browser");
		builder.setSmallIcon(android.R.drawable.alert_light_frame);
		builder.setOngoing(true);
		
		startForeground(SERVICE_ID, builder.build());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		final Handler handler = new Handler();
		
		// Create timer to fire every 500ms to handle security
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() 
		{
			@Override
			public void run() 
			{
				// User handler to hide application and detect system changes
				handler.post(new Runnable() 
				{
					@Override
					public void run() 
					{
						hideApplications();
						checkSettings();
					}
				});
			}
		}, 0, 500);
		
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		mTimer.cancel();
	}

	/**
	 * Hide any application that is in the foreground and is not
	 * in the white-list, and collapse the status bar.
	 */
	private void hideApplications()
	{
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = am.getRunningTasks(1);
       
        if(!tasks.isEmpty())
        {
            ComponentName topActivity = tasks.get(0).topActivity;
           
            String packageName = topActivity.getPackageName();
            
            if (!packageName.equals(getPackageName()) && !mApplicationWhitelist.contains(packageName)) 
            {
            	//We hide the activity, by bringing the BrowserActivity back in front.
            	Intent intent = new Intent(this, BrowserActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		startActivity(intent);
            		
        		//Visual indicator that we are hiding an app.
        		Toast.makeText(this, "Blocked " + packageName, Toast.LENGTH_SHORT).show();
            }
        }

		try
		{
			final Object service = getSystemService("statusbar");
			Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");

			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
				final Method collapse = statusbarManager.getMethod("collapse");
				collapse.setAccessible(true);
				collapse.invoke(service);
			}
			else {
				final Method collapse = statusbarManager.getMethod("collapsePanels");
				collapse.setAccessible(true);
				collapse.invoke(service);
			}
		}
		catch(Exception ex)
		{
			Log.e(TAG, "Failed to collapse status bar");
		}
		
		List<RunningAppProcessInfo> appList = am.getRunningAppProcesses();
		
		for (RunningAppProcessInfo info : appList) {
			am.killBackgroundProcesses(info.processName);
		}
		
		appList = am.getRunningAppProcesses();
		
		for (RunningAppProcessInfo info : appList) {
			if (mAppBlacklist.contains(info.processName)) {
				Intent intent = new Intent(super.getResources().getString(R.string.intent_black_logtag));
				LocalBroadcastManager.getInstance(super.getApplicationContext()).sendBroadcast(intent);
			}
		}
		
		
	}
	
	/**
	 * Check for changes to key system settings (typically settings which don't fire Intents/Events
	 * when they are changed).
	 */
	private void checkSettings() {
		// Clear the clipboard, regardless of contents
		mClipboardManager.setPrimaryClip(NEW_PLAIN_TEXT);
		
		// Check if the keyboard IME has changed, fire event and let someone else deal with the change
		String curKeyboard = KeyboardUtil.getKeyboardPackage(super.getContentResolver());
		if (!curKeyboard.equals(mLastKeyboard)) {
			mLastKeyboard = curKeyboard;
			Intent intent = new Intent(super.getResources().getString(R.string.intent_keyboardchange));
			LocalBroadcastManager.getInstance(super.getApplicationContext()).sendBroadcast(intent);
		}
		
		// Check if microphone mute flag changed, fire event and let someone else deal with the change
		boolean micMute = mAudioManager.isMicrophoneMute();
		if (mMicMute != micMute) {
			mMicMute = micMute;
			Intent intent = new Intent(super.getResources().getString(R.string.intent_micmutechanged));
			LocalBroadcastManager.getInstance(super.getApplicationContext()).sendBroadcast(intent);
		}
	}
}
