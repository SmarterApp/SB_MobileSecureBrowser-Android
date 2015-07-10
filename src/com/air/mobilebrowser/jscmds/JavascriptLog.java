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

import android.graphics.Color;
import android.widget.TextView;

import com.air.mobilebrowser.BrowserActivity;
import com.air.mobilebrowser.DeviceStatus;
import com.air.mobilebrowser.R;

/**
 * {@link JSCmd} implementation that handles logging
 * a message in the device console (debug mode)
 */
public class JavascriptLog extends JSCmd {

	private static final String CMD_JAVASCRIPT_LOG = "cmdJavascriptLog";
	
	public JavascriptLog(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_JAVASCRIPT_LOG, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		final String TYPE = "type";
		final String VALUE = "value";

		final String ERROR = "error";
		final String WARN = "warn";
		final String DEBUG = "debug";

		try 
		{
			String type = parameters.getString(TYPE);
			String value = parameters.getString(VALUE);

			if (super.getActivity().isDebugEnabled()) {
				TextView consoleView = (TextView)super.getActivity().findViewById(R.id.air_jsconsole_view);
	
				if(consoleView != null)
				{
					if(type.equalsIgnoreCase(ERROR))
					{
						super.getActivity().logMessage(consoleView, type, value, Color.RED);
					}
					else if(type.equalsIgnoreCase(WARN))
					{
						super.getActivity().logMessage(consoleView, type, value, Color.YELLOW);
					}
					else if(type.equalsIgnoreCase(DEBUG))
					{
						super.getActivity().logMessage(consoleView, type, value, 0xFF5E2605); //and acceptable brown
					}
					else //info
					{
						super.getActivity().logMessage(consoleView, type, value, Color.BLUE);
					}
				}
			}
		} 
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

}
