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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives keyboard,mic,and blacklist status change broadcasts and forwards them
 * to its handler object.
 *
 */
public class SBReceiver extends BroadcastReceiver {

	private WeakReference<BrowserActivity> contextRef = null;
	
	public SBReceiver(BrowserActivity activity) {
		contextRef = new WeakReference<BrowserActivity>(activity);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		BrowserActivity activity = contextRef.get();
		if (activity != null) {
			String action = intent.getAction();
			if (activity.getResources().getString(R.string.intent_keyboardchange).equals(action)) {
				activity.onKeyboardChanged();
			}
			else if (activity.getResources().getString(R.string.intent_micmutechanged).equals(action)) {
				activity.onMicChanged();
			}
			else if (activity.getResources().getString(R.string.intent_black_logtag).equals(action)) {
				activity.onBlackLogTag();
			}
			else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				// do not do anything
			}
			else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				// do not do anything
			}
		}
	}

}
