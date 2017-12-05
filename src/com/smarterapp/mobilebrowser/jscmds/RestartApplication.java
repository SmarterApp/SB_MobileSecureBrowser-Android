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

import android.content.Intent;

public class RestartApplication extends JSCmd {

	public static final String CMD_RESTART = "cmdRestartApplication";
	
	public RestartApplication(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_RESTART, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		super.getActivity().getHandler().post(new Runnable(){
			@Override
			public void run() {
				
				Intent refresh = new Intent(RestartApplication.this.getActivity(), BrowserActivity.class);
				RestartApplication.this.getActivity().finish();
				RestartApplication.this.getActivity().startActivity(refresh);
			}
		});
		
		return null;
	}
	
}

