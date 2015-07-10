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

import android.content.pm.ActivityInfo;

import com.air.mobilebrowser.BrowserActivity;
import com.air.mobilebrowser.DeviceStatus;

/**
 * {@link JSCmd} implementation that handles locking
 * the device orientation. 
 */
public class OrientationLock extends JSCmd {

	public static final String CMD_LOCK_ORIENTATION = "cmdLockOrientation";
	
	public OrientationLock(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_LOCK_ORIENTATION, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {

		String requestedOrientation = null;
		
		try
		{
			requestedOrientation = parameters.getString(JSKeys.ORIENTATION);
		}
		catch(JSONException e) { /* */ }
		
		if(requestedOrientation != null)
		{
			if(requestedOrientation.equalsIgnoreCase("landscape"))
			{
				super.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
				super.getDeviceStatus().lockedOrientation = "landscape";
			}
			else if(requestedOrientation.equalsIgnoreCase("portrait"))
			{
				super.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
				super.getDeviceStatus().lockedOrientation = "portrait";
			}
			else
			{
				super.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
				super.getDeviceStatus().lockedOrientation = "none";
			}
		}
		
		JSONObject jsonToSend = new JSONObject();
		
		super.setRequestIdentifier(parameters, jsonToSend);
		jsonToSend.put(JSKeys.ORIENTATION, super.getDeviceStatus().orientation);
		jsonToSend.put(JSKeys.LOCKED_ORIENTATION, super.getDeviceStatus().lockedOrientation);

		
		return new JSCmdResponse(JSNTVCmds.NTV_ON_ORIENTATION_LOCK, jsonToSend);
	}

}
