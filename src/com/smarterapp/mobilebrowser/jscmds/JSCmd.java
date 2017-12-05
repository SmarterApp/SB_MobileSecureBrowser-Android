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
 * Abstract class for handling various commands
 * that may be executed via javascript. Subclass implementations
 * override the handle() method to complete their custom work.
 */
public abstract class JSCmd {

	private DeviceStatus mDeviceStatus;
	private BrowserActivity mActivity;
	private String mCmd;
	
	public JSCmd(String cmd, BrowserActivity activity, DeviceStatus deviceStatus) {
		this.mCmd = cmd;
		this.mDeviceStatus = deviceStatus;
		this.mActivity = activity;
	}
	
	public abstract JSCmdResponse handle(JSONObject parameters) throws JSONException;

	/**
	 * @return the mDeviceStatus
	 */
	public DeviceStatus getDeviceStatus() {
		return mDeviceStatus;
	}
	
	public BrowserActivity getActivity() {
		return mActivity;
	}
	
	public String getCmd() {
		return mCmd;
	}
	
	public String getRequestIdentifier(JSONObject parameters) {
		String requestIdentifier = null;

		try
		{
			requestIdentifier = parameters.getString(JSKeys.REQUEST_IDENTIFIER);
		} 
		catch(Exception e) { /* */ }
		
		return requestIdentifier;
	}

	public void setRequestIdentifier(JSONObject parameters, JSONObject jsonToSend) throws JSONException {
		String id = getRequestIdentifier(parameters);
		if (id != null) {
			jsonToSend.put(JSKeys.REQUEST_IDENTIFIER, id);
		}
	}
}
