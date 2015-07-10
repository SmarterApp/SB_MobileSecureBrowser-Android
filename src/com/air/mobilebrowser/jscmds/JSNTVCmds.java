/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser.jscmds;

/**
 * Static collection of known javascript response methods.
 */
public interface JSNTVCmds {
	public static final String NTV_RETURN_FROM_BACKGROUND 	= "ntvApplicationReturnFromBackground";
	public static final String NTV_ENTER_BACKGROUND			= "ntvApplicationEnterBackground";
	public static final String NTV_ON_RECORDING_COMPLETE	= "ntvOnRecordingComplete";
	public static final String NTV_RUNNING_PROCESS_UPDATE	= "ntvOnRunningProcessesUpdated";
	public static final String NTV_ON_TTS_ENABLED			= "ntvOnTextToSpeechEnabled";
	public static final String NTV_ON_DEVICE_READY			= "ntvOnDeviceReady";
	public static final String NTV_ON_SET_DEFAULT_URL		= "ntvOnSetDefaultURL";
	public static final String NTV_ON_CONNECTIVITY_CHANGE	= "ntvOnConnectivityChanged";
	public static final String NTV_ON_ORIENTATION_CHANGE	= "ntvOnOrientationChanged";
	public static final String NTV_ON_ORIENTATION_LOCK		= "ntvOnOrientationLockChanged";
	public static final String NTV_ON_UNSUPPORTED_REQUEST 	= "ntvOnRequestNotSupported";
	public static final String NTV_ON_KEYBOARD_CHANGED		= "ntvOnKeyboardChanged";
	public static final String NTV_ON_MIC_MUTE_CHANGED 		= "ntvOnMicMuteChanged";
	public static final String NTV_MINI_APP_LAUNCHED 		= "ntvMiniAppLaunched";
	public static final String NTV_ON_RECORDER_INIT			= "ntvOnRecorderInitialized";
	public static final String NTV_ON_RECORDER_UPDATE 		= "ntvOnRecorderUpdate";
	public static final String NTV_ON_PLAYBACK_STATE_CHANGE = "ntvOnPlaybackStateChanged";
	public static final String NTV_ON_CLIPBOARD_CHANGE		= "ntvOnClipboardContentsChanged";
	
	public static final String NTV_ON_AUDIO_FILE_LIST_RETRIEVED = "ntvOnAudioFileListRetrieved";
	public static final String NTV_ON_AUDIO_FILE_DATA_RETRIEVED = "ntvOnAudioFileDataRetrieved";
	
	public static final String NTV_ON_TEXT_SELECTED			= "ntvOnTextSelected";
	
}
