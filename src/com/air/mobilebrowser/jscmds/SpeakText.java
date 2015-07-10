/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser.jscmds;

import org.json.JSONException;
import org.json.JSONObject;

import com.air.mobilebrowser.BrowserActivity;
import com.air.mobilebrowser.DeviceStatus;
import com.air.mobilebrowser.TTSPlayer;

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
		float pitch = 100, rate = 100;
		JSONObject options;
		
		try
		{
			options = parameters.getJSONObject("options");
			
			requestedTextToSpeak = parameters.getString(JSKeys.TEXT_TO_SPEAK);
			requestIdentifier = super.getRequestIdentifier(parameters);
			
			language = options.getString("language");
			pitch = (float) options.getDouble("pitch");
			rate = (float) options.getDouble("rate");
		}
		catch(JSONException e) { /* */ }
		
		if (!super.getDeviceStatus().ttsEngineStatus.equals(DeviceStatus.TTS_ENGINE_STATUS_UNAVAILABLE) && requestedTextToSpeak != null) 
		{	
			super.getDeviceStatus().ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_PLAYING;
			
			mTTSPlayer.startPlayback(requestedTextToSpeak, language, pitch, rate, requestIdentifier);			
		}
		
		JSONObject jsonToSend = new JSONObject();
		
		jsonToSend.put(JSKeys.TTS_ENABLED, super.getDeviceStatus().ttsEnabled);			
		jsonToSend.put(JSKeys.TTS_ENGINE_STATUS, super.getDeviceStatus().ttsEngineStatus);
		
		super.setRequestIdentifier(parameters, jsonToSend);

		return new JSCmdResponse(JSNTVCmds.NTV_ON_TTS_ENABLED, jsonToSend);
	}

}
