//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import com.smarterapp.mobilebrowser.softkeyboard.KeyboardUtil;

import android.content.Context;
import android.media.AudioManager;

/**
 * Contains all original settings when application is launched.
 * 
 * @author Cody Henthorne
 */
public class OrigSettings {

	/** Singleton instance */
	private static OrigSettings instance;
	
	/** Keyboard IME package on start */
	private String keyboard;
	/** Microphone mute status */
	private boolean micMute;
	
	/**
	 * Collect and save the original settings.
	 * 
	 * @param context - Application context
	 */
	private OrigSettings(Context context) {
		// Save keyboard
		keyboard = KeyboardUtil.getKeyboardPackage(context.getContentResolver());
		
		// Save mic mute status
		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		micMute = am.isMicrophoneMute();
	}
	
	/**
	 * Initialize the original settings.
	 * 
	 * @param context - Application context
	 * @return singleton instance of OrigSettings
	 */
	public static OrigSettings initOrigSettings(Context context) {
		instance = new OrigSettings(context);
		return instance;
	}
	
	/**
	 * Get instance of OrigSettings.
	 * 
	 * @return singleton instance of OrigSettings
	 */
	public static OrigSettings getInstance() {
		return instance;
	}
	
	/**
	 * Get original keyboard IME package.
	 * 
	 * @return keyboard IME package
	 */
	public String getKeyboard() {
		return keyboard;
	}

	/**
	 * Get original microphone muted status.
	 * 
	 * @return microphone muted status
	 */
	public boolean isMicMuted() {
		return micMute;
	}
}
