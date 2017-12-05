//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.jscmds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smarterapp.mobilebrowser.BrowserActivity;
import com.smarterapp.mobilebrowser.DeviceStatus;

/**
 * {@link JSCmd} implementation that handles retreiving
 * the list of processes running on the device.
 */
public class GetProcesses extends JSCmd {

	private static final String CMD_GET_NATIVE_PROCESSES = "cmdGetNativeProcesses";
	
	public GetProcesses(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_GET_NATIVE_PROCESSES, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		JSONObject jsonToSend = new JSONObject();

		jsonToSend.put(JSKeys.RUNNING_PROCESSES, new JSONArray(super.getDeviceStatus().processes()));
		super.setRequestIdentifier(parameters, jsonToSend);
		
		return new JSCmdResponse(JSNTVCmds.NTV_RUNNING_PROCESS_UPDATE, jsonToSend);
	}

}
