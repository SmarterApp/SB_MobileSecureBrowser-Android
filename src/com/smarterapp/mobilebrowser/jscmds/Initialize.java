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

/**
 * {@link JSCmd} implementation that handles initializing
 * the browser.
 */
public class Initialize extends JSCmd {

	public static final String CMD_INITIALIZE = "cmdInitialize";
	
	public Initialize(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_INITIALIZE, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {

		return new JSCmdResponse(JSNTVCmds.NTV_ON_DEVICE_READY, super.getDeviceStatus().toJSONObject());
	}

}
