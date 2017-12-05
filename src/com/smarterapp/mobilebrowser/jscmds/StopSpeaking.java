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
 * {@link JSCmd} implementation that handles stopping text to speech.
 */
public class StopSpeaking extends JSCmd {

	private static final String CMD_STOP_SPEAKING = "cmdStopSpeaking";

	private TTSPlayer mTTSPlayer;
	
	public StopSpeaking(BrowserActivity activity, DeviceStatus deviceStatus, TTSPlayer ttsPlayer) {
		super(CMD_STOP_SPEAKING, activity, deviceStatus);
		this.mTTSPlayer = ttsPlayer;
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		
		if (super.getDeviceStatus().ttsEngineStatus.equals(DeviceStatus.TTS_ENGINE_STATUS_PLAYING)) 
		{	
			super.getDeviceStatus().ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_IDLE;
		}
		mTTSPlayer.stopPlayback();
		
		JSONObject jsonToSend = new JSONObject();
		
		jsonToSend.put(JSKeys.TTS_ENABLED, super.getDeviceStatus().ttsEnabled);			
		jsonToSend.put(JSKeys.TTS_ENGINE_STATUS, super.getDeviceStatus().ttsEngineStatus);
		
		super.setRequestIdentifier(parameters, jsonToSend);

		return new JSCmdResponse(JSNTVCmds.NTV_ON_TTS_ENABLED, jsonToSend);
	}

}
