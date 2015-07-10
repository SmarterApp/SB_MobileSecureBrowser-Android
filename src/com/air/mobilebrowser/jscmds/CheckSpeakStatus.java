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

/**
 * {@link JSCmd} implementation that handles checking
 * the status of text to speech.
 */
public class CheckSpeakStatus extends JSCmd {

	private static final String CMD_CHECK_SPEAK_STATUS = "cmdCheckSpeakStatus";
	
	public CheckSpeakStatus(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_CHECK_SPEAK_STATUS, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {		
		JSONObject jsonToSend = new JSONObject();
		
		jsonToSend.put(JSKeys.TTS_ENABLED, super.getDeviceStatus().ttsEnabled);			
		jsonToSend.put(JSKeys.TTS_ENGINE_STATUS, super.getDeviceStatus().ttsEngineStatus);
		
		super.setRequestIdentifier(parameters, jsonToSend);

		return new JSCmdResponse(JSNTVCmds.NTV_ON_TTS_ENABLED, jsonToSend);
	}

}
