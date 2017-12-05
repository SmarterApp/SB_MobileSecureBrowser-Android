//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.jscmds;

/**
 * Static collection of keys used in the payloads
 * of the various javascript command parameters passed
 * back and forth between the native applicaiton and web. 
 */
public interface JSKeys {
	/** Command Parameter Keys */
	public static final String REQUEST_IDENTIFIER			= "identifier";
	public static final String RUNNING_PROCESSES 			= "runningProcesses";
	public static final String TTS_ENABLED					= "enabled";
	public static final String TTS_ENGINE_STATUS 			= "ttsEngineStatus";
	public static final String TTS_PAUSE_RESUME_ENABLED		= "pauseResumeEnabled";
	public static final String VERSION						= "version";
	public static final String URL 							= "url";
	public static final String ORIENTATION 					= "orientation";
	public static final String LOCKED_ORIENTATION 			= "lockedOrientation";
	public static final String TEXT_TO_SPEAK 				= "textToSpeak";
	public static final String STATUS 						= "status";
	public static final String KEYBOARD 					= "keyboard";
	public static final String RECORDER_STATE 				= "state";
	public static final String RECORDER_ERROR 				= "error";
	
	
	public static final String REC_UPTYPE = "updateType";
	public static final String REC_UPTYPE_START = "START";
	public static final String REC_UPTYPE_INPROGRESS = "INPROGRESS";
	public static final String REC_UPTYPE_END = "END";
	public static final String REC_UPTYPE_ERROR = "ERROR";
	
	public static final String REC_KBYTES_REC = "kilobytesRecorded";
	public static final String REC_SECS_REC = "secondsRecorded";
	public static final String REC_ERROR = "error";
	public static final String REC_DATA = "data";
	
	public static final String PB_STATE = "playbackState";
	
	public static final String EVENT = "event";
	public static final String EVENT_KEYBOARD_CHANGED = "event_keyboard_changed";
	public static final String EVENT_CLIPBOARD_CHANGED = "event_clipboard_changed";
	public static final String EVENT_RETURN_FROM_BACKGROUND = "event_return_from_background";
	public static final String EVENT_ENTER_BACKGROUND = "event_enter_background";
}
