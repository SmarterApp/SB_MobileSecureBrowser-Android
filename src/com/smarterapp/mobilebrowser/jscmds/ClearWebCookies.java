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

import android.webkit.CookieManager;

/**
 * {@link JSCmd} implementation that handles clearing any cookies from the app.
 */

public class ClearWebCookies extends JSCmd {
	
	public static final String CMD_CLEAR_WEB_COOKIES = "cmdRequestClearCookies";

	public ClearWebCookies(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_CLEAR_WEB_COOKIES, activity, deviceStatus);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		CookieManager.getInstance().removeAllCookie();
		return null;
	}

}
