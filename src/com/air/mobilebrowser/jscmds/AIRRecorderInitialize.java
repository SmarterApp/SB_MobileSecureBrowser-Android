/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser.jscmds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.air.mobilebrowser.AIRRecorder;
import com.air.mobilebrowser.AIRRecorderCapability;
import com.air.mobilebrowser.AIRRecorderStatus;
import com.air.mobilebrowser.BrowserActivity;
import com.air.mobilebrowser.DeviceStatus;
import com.air.mobilebrowser.JSONUtil;

/**
 * {@link JSCmd} implementation that handles initializing
 * the audio recorder.
 */
public class AIRRecorderInitialize extends JSCmd {

	public static final String CMD_AIR_REC_INITIALIZE = "cmdInitializeRecorder";
	
	public AIRRecorderInitialize(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_AIR_REC_INITIALIZE, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		AIRRecorder recorder = AIRRecorder.getInstance();

		AIRRecorderStatus status = recorder.initialize(super.getActivity());
		
		JSONObject jsonToSend = new JSONObject();

		super.setRequestIdentifier(parameters, jsonToSend);
		
		if (status == AIRRecorderStatus.ERROR) {
			jsonToSend.put(JSKeys.RECORDER_STATE, "ERROR");
			jsonToSend.put(JSKeys.RECORDER_ERROR, recorder.getError());	
		}
		else {
			jsonToSend.put(JSKeys.RECORDER_STATE, "READY");
			JSONObject caps = new JSONObject();
			caps.put("isAvailable", true);
			JSONArray devices = new JSONArray();
			for (AIRRecorderCapability cap : recorder.getCapabilities()) {
				JSONObject device = new JSONObject();
				device.put("id", cap.getId());
				device.put("description", cap.getDescription());
				device.put("sampleSizes", JSONUtil.toJSONArray(cap.getSampleSizes()));
				device.put("sampleRates", JSONUtil.toJSONArray(cap.getSampleRates()));
				device.put("channelCounts", JSONUtil.toJSONArray(cap.getChannels()));
				device.put("formats", JSONUtil.toJSONArray(cap.getFormats()));
				devices.put(device);
			}
			caps.put("supportedInputDevices", devices);
			jsonToSend.put("capabilities", caps);
		}

		return new JSCmdResponse(JSNTVCmds.NTV_ON_RECORDER_INIT, jsonToSend);
	}

}
