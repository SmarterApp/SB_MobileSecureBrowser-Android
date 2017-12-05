//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.jscmds;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.smarterapp.mobilebrowser.BrowserActivity;
import com.smarterapp.mobilebrowser.DeviceStatus;

import android.app.Activity;
import android.util.Log;

/**
 * {@link JSCmd} implementation that handles clearing the apps cache directory.
 */

public class ClearWebCache extends JSCmd {
	
	public static final String CMD_CLEAR_WEB_CACHE = "cmdRequestClearWebCache";
	private Activity mActivity;
	public ClearWebCache(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_CLEAR_WEB_CACHE, activity, deviceStatus);
		this.mActivity = activity;
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		File dir = this.mActivity.getCacheDir();

        if(dir!= null && dir.isDirectory()){
            try{
            File[] children = dir.listFiles();
            if (children.length >0) {
                for (int i = 0; i < children.length; i++) {
                    File[] temp = children[i].listFiles();
                    for(int x = 0; x<temp.length; x++)
                    {
                        temp[x].delete();
                    }
                }
            }
            }catch(Exception e)
            {
                Log.e("Cache", "failed cache clean");
            }
        }
		return null;
	}

}
