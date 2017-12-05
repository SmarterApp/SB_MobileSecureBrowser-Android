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
 * {@link JSCmd} implementation that handles speaking
 * back text.
 */
public class SpeakText extends JSCmd {

	private static final String CMD_SPEAK_TEXT = "cmdSpeakText";
	
	private TTSPlayer mTTSPlayer;
	
	public SpeakText(BrowserActivity activity, DeviceStatus deviceStatus, TTSPlayer ttsPlayer) 
	{
		super(CMD_SPEAK_TEXT, activity, deviceStatus);
		this.mTTSPlayer = ttsPlayer;
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException 
	{
		String requestedTextToSpeak = null;
		String requestIdentifier = null;
		String language = null;

		float pitch = 10, rate = 10, volume = 10;	// set the default pitch, rate and volume to 10
		JSONObject options;
		
		try
		{
			options = parameters.getJSONObject("options");
			
			requestedTextToSpeak = parameters.getString(JSKeys.TEXT_TO_SPEAK);
			if (parameters.has(JSKeys.TTS_PAUSE_RESUME_ENABLED)) {
				super.getDeviceStatus().ttsPauseResumeEnabled = parameters.getBoolean(JSKeys.TTS_PAUSE_RESUME_ENABLED);
			}
			requestIdentifier = super.getRequestIdentifier(parameters);
			
			// retrieve tts speak options
			// if language is not given, use English by default
			if (options.has("id")) {
			        language = options.getString("id");
			} else {
			        language = "en";
			}
			if (options.has("pitch")) {
			        pitch = (float) options.getDouble("pitch");
			}
			if (options.has("rate")) {
			        rate = (float) options.getDouble("rate");
			}
			if (options.has("volume")) {
			        volume = (float) options.getDouble("volume");
			}
		}
		catch(JSONException e) { /* */ }
		
		if (!super.getDeviceStatus().ttsEngineStatus.equals(DeviceStatus.TTS_ENGINE_STATUS_UNAVAILABLE) && requestedTextToSpeak != null) 
		{	
			super.getDeviceStatus().ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_PLAYING;
			
			mTTSPlayer.startPlayback(requestedTextToSpeak, language, pitch, rate, volume, requestIdentifier, super.getDeviceStatus().ttsPauseResumeEnabled);			
		}
		
		JSONObject jsonToSend = new JSONObject();
		
		jsonToSend.put(JSKeys.TTS_ENABLED, super.getDeviceStatus().ttsEnabled);			
		jsonToSend.put(JSKeys.TTS_ENGINE_STATUS, super.getDeviceStatus().ttsEngineStatus);
		
		super.setRequestIdentifier(parameters, jsonToSend);

		return new JSCmdResponse(JSNTVCmds.NTV_ON_TTS_ENABLED, jsonToSend);
	}

}
