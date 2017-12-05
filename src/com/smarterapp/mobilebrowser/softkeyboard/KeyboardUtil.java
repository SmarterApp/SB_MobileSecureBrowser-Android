//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.softkeyboard;

import java.util.Timer;
import java.util.TimerTask;

import com.smarterapp.mobilebrowser.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;

/**
 * Utility for handling checking and switching the keyboard (IME) to a specific
 * IME.
 * 
 * @author Cody Henthorne
 */
public class KeyboardUtil {

	/**
	 * Force the user to select the provided target keyboard (IME).
	 * 
	 * @param activity - Parent/Current activity forcing the change
	 * @param targetKeyboard - Package name of IME to force
	 * @param callback - Callback to indicate when proper keyboard/IME selected (can be null)
	 * @param showWarning - Flag indicating if a warning should be displayed
	 */
	public static void forceKeyboard(Activity activity, String targetKeyboard, IKeyboardSelected callback, boolean showWarning) {
		boolean correctKeyboard = isCorrectKeyboard(activity.getContentResolver(), targetKeyboard);
		if (!correctKeyboard) {
			startKeyboardWait(activity, targetKeyboard, callback, showWarning);
		}
	}
	
	/**
	 * Notifies that the incorrect keyboard is selected and forces the IME selection dialog to
	 * appear until the proper IME is selected.
	 * 
	 * @param activity Parent/Current activity forcing the change
	 * @param targetKeyboard - Package name of IME to force
	 * @param callback - Callback to indicate when proper keyboard/IME selected (can be null)
	 * @param showWarning - Flag indicating if a warning should be displayed
	 */
	public static void startKeyboardWait(final Activity activity, final String targetKeyboard, final IKeyboardSelected callback, boolean showWarning) {
		if (showWarning) {
			//Notify user of what they need to
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(R.string.dialog_keyboard_alert).setTitle(R.string.dialog_keyboard_alert_title);
			builder.setPositiveButton("Close", null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
		/*
		 * Since we can't select programmatically, and cannot detect when the dialog is closed,
		 * a timer is user to poll the current keyboard and redisplay the selection dialog if
		 * necessary.
		 */
		final Handler handler = new Handler();
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() 
		{
			@Override
			public void run() 
			{
				handler.post(new Runnable() 
				{
					@Override
					public void run() 
					{
						if (!isCorrectKeyboard(activity.getContentResolver(), targetKeyboard)) {
							if (activity.hasWindowFocus()) {
								showInputMethodPicker(activity.getApplicationContext());
							}
						}
						else {
							timer.cancel();
							if (callback != null) {
								callback.keyboardSelected();
							}
						}
					}
				});
			}
		}, 0, 500);
	}

	/**
	 * Check if the current IME is the correct one.
	 * 
	 * @param resolver - Resolver use to get current IME
	 * @param targetKeyboard - Desired/target IME package name
	 * @return
	 */
	public static boolean isCorrectKeyboard(ContentResolver resolver, String targetKeyboard) {
		String defaultIME = Settings.Secure.getString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD);
		return defaultIME.equals(targetKeyboard);
	}
	
	/**
	 * Display the keyboard/IME selection dialog.
	 * 
	 * @param context - Application context
	 */
	public static void showInputMethodPicker(Context context) {
        InputMethodManager imeManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE); 
        if (imeManager != null) {
            imeManager.showInputMethodPicker();
        }
    }
	
	/**
	 * Hide the keyboard from the current view item.
	 * 
	 * @param context - Application context
	 * @param iBinder - Current view item causing the keyboard to show ({@link android.view.View#getWindowToken()})
	 */
	public static void hideKeyboard(Context context, IBinder iBinder) {
		InputMethodManager imeManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imeManager.hideSoftInputFromWindow(iBinder, 0);
	}

	/**
	 * Get the current keyboard package.
	 * 
	 * @param resolver - Resolver use to get current IME
	 * @return Current IME package
	 */
	public static String getKeyboardPackage(ContentResolver resolver) {
		return Settings.Secure.getString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD);
	}
	
	/**
	 * Display an alert asking the user if they would like to view
	 * the device input settings.
	 * @param context the context used to create the dialog
	 * @param cancelListenter an optional cancel listener for the dialog.
	 */
	public static void showInputChangedExitDialog(final Context context, final DialogInterface.OnDismissListener dismissListener){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Keyboard Changed").setMessage("Would you like to view keyboard settings?");
		builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				context.startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
			}
		});
		
		builder.setNegativeButton("Cancel", null);
		AlertDialog dialog = builder.show();
		dialog.setOnDismissListener(dismissListener);
	}
	
}