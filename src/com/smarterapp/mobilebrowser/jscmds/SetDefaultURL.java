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
 * {@link JSCmd} implementation that handles setting the
 * default url that loads on application start.
 */
public class SetDefaultURL extends JSCmd {

	public static final String CMD_SET_DEFAULT_URL = "cmdSetDefaultURL";
	
	public SetDefaultURL(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_SET_DEFAULT_URL, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		String newDefault = null;
		
		try
		{
			newDefault = parameters.getString(JSKeys.URL);
		} 
		catch(JSONException e) { /* */ }

		JSONObject jsonToSend = new JSONObject();

		if(newDefault != null)
		{
			getDeviceStatus().setDefaultURL(newDefault);
			jsonToSend.put(JSKeys.URL, newDefault);
		} else {
			getDeviceStatus().setDefaultURL(null);
			newDefault = getDeviceStatus().getDefaultURL();
			jsonToSend.put(JSKeys.URL, newDefault);
		}
		super.setRequestIdentifier(parameters, jsonToSend);

		return new JSCmdResponse(JSNTVCmds.NTV_ON_SET_DEFAULT_URL, jsonToSend);
	}

}
