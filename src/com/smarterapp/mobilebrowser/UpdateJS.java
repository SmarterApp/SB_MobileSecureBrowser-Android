//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import java.lang.ref.WeakReference;

import org.json.JSONObject;

/**
 * Runnable implementation that executes a javascript command
 * on the provided BrowserActivity.
 */
public class UpdateJS implements Runnable {
	private WeakReference<BrowserActivity> mActivity;
	private JSONObject mParams;
	private String mCmd;

	public UpdateJS(BrowserActivity browserActivity, String cmd, JSONObject params) {
		this.mActivity = new WeakReference<BrowserActivity>(browserActivity);
		this.mParams = params;
		this.mCmd = cmd;
	}

	@Override
	public void run() {
		if (mActivity.get() != null) {
			mActivity.get().executeAIRMobileFunction(mCmd, mParams.toString());
		}
	}
}