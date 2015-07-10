/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser.jscmds;

import org.json.JSONObject;

/**
 * Wrapper around a javascript command response.
 */
public class JSCmdResponse {

	private String ntvCmd;
	private JSONObject parameters;

	public JSCmdResponse(String ntvCmd, JSONObject parameters) {
		this.ntvCmd = ntvCmd;
		this.parameters = parameters;
	}

	public String getParameters() {
		return parameters.toString();
	}

	public String getNTVCmd() {
		return ntvCmd;
	}

}
