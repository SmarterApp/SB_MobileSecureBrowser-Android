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
 * {@link JSCmd} implementation that handles exiting
 * the application.
 */
public class ExitApplication extends JSCmd {

	public static final String CMD_EXIT = "cmdExitApplication";
	
	public ExitApplication(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_EXIT, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		super.getActivity().getHandler().post(new Runnable(){
			@Override
			public void run() {
				ExitApplication.this.getActivity().cleanup();
			}
		});
		
		return null;
	}

}
