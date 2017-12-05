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

import com.smarterapp.mobilebrowser.AIRRecorder;
import com.smarterapp.mobilebrowser.BrowserActivity;
import com.smarterapp.mobilebrowser.DeviceStatus;

/**
 * {@link JSCmd} implementation that handles resuming
 * audio recorder playback.
 */
public class AIRRecorderResumePlayback extends JSCmd {

	public static final String CMD = "cmdResumeAudioPlayback";
	
	public AIRRecorderResumePlayback(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		AIRRecorder recorder = AIRRecorder.getInstance();
		if (!recorder.isInit()) {
			return null;
		}
		
		JSONObject jsonToSend = new JSONObject();
		super.setRequestIdentifier(parameters, jsonToSend);
		if (recorder.isPlaying()) {
			recorder.resumePlayback();
			jsonToSend.put(JSKeys.PB_STATE, "resumed");
		}
		else {
			jsonToSend.put(JSKeys.PB_STATE, "error");
		}
		return new JSCmdResponse(JSNTVCmds.NTV_ON_PLAYBACK_STATE_CHANGE, jsonToSend);
	}

}
