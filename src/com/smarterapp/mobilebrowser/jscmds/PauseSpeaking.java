//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.jscmds;

import org.json.JSONException;
import org.json.JSONObject;

import com.smarterapp.mobilebrowser.BrowserActivity;
import com.smarterapp.mobilebrowser.DeviceStatus;
import com.smarterapp.mobilebrowser.TTSPlayer;

/**
 * {@link JSCmd} implementation that handles pausing
 * text to speech playback.
 */
public class PauseSpeaking extends JSCmd {

	private static final String CMD_PAUSE_SPEAKING = "cmdPauseSpeaking";
	private TTSPlayer mTTSPlayer;
	
	public PauseSpeaking(BrowserActivity activity, DeviceStatus deviceStatus, TTSPlayer ttsPlayer) {
		super(CMD_PAUSE_SPEAKING, activity, deviceStatus);
		this.mTTSPlayer = ttsPlayer;
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		
		if (super.getDeviceStatus().ttsPauseResumeEnabled) {
			this.mTTSPlayer.pausePlayback();
			super.getDeviceStatus().ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_PAUSED;
		}
		return null;
	}

}
