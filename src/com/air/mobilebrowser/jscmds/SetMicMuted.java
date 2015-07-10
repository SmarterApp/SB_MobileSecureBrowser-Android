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

import android.media.AudioManager;

import com.air.mobilebrowser.BrowserActivity;
import com.air.mobilebrowser.DeviceStatus;

/**
 * {@link JSCmd} implementation that handles muting/un-muting
 * the device microphone.
 */
public class SetMicMuted extends JSCmd {

	private static final String CMD_SET_MIC_MUTED = "cmdSetMicMuted";
	private AudioManager mAudioManager;
	
	public SetMicMuted(BrowserActivity activity, DeviceStatus deviceStatus, AudioManager audioManager) {
		super(CMD_SET_MIC_MUTED, activity, deviceStatus);
		this.mAudioManager = audioManager;
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		boolean micMuted = super.getDeviceStatus().micMuted;
		try
		{
			micMuted = parameters.getBoolean(DeviceStatus.MICMUTED_KEY);
		}
		catch(JSONException e) { /* */ }

		// this is what we are expecting, helps with notification code in race condition
		super.getDeviceStatus().micMuted = micMuted;
		mAudioManager.setMicrophoneMute(micMuted);
		// Make sure device status matches the actual
		super.getDeviceStatus().micMuted = mAudioManager.isMicrophoneMute();
		
		JSONObject jsonToSend = new JSONObject();
		jsonToSend.put(DeviceStatus.MICMUTED_KEY, super.getDeviceStatus().micMuted);
		super.setRequestIdentifier(parameters, jsonToSend);
		
		return new JSCmdResponse(JSNTVCmds.NTV_ON_MIC_MUTE_CHANGED, jsonToSend);
	}

}
