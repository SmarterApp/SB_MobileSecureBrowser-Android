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

import com.air.mobilebrowser.AIRRecorder;
import com.air.mobilebrowser.BrowserActivity;
import com.air.mobilebrowser.DeviceStatus;

/**
 * {@link JSCmd} implementation that handles stopping
 * audio recorder playback.
 */
public class AIRRecorderStopPlayback extends JSCmd {

	public static final String CMD = "cmdStopAudioPlayback";
	
	public AIRRecorderStopPlayback(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		//calling this will trigger the listener registered in StartPlayback
		AIRRecorder recorder = AIRRecorder.getInstance();
		if (!recorder.isInit()) {
			return null;
		}
		recorder.stopPlayback();
		return null;
	}

}
