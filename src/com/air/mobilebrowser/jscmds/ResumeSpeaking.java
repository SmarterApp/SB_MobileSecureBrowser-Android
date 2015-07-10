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
 * {@link JSCmd} implementation that handles resuming text
 * to speech playback.
 */
public class ResumeSpeaking extends JSCmd {

	private static final String CMD_RESUME_SPEAKING = "cmdResumeSpeaking";
	private TTSPlayer mTTSPlayer;
	
	public ResumeSpeaking(BrowserActivity activity, DeviceStatus deviceStatus, TTSPlayer ttsPlayer) {
		super(CMD_RESUME_SPEAKING, activity, deviceStatus);
		this.mTTSPlayer = ttsPlayer;
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		
		this.mTTSPlayer.resumePlayback();
		return null;
	}

}
