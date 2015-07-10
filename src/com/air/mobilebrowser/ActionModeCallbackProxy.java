/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser;

import java.lang.ref.WeakReference;

import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;

public class ActionModeCallbackProxy implements Callback {
	
	private WeakReference<Callback> mCallbackRef = null;
	
	public ActionModeCallbackProxy(Callback callback) {
		this.mCallbackRef = new WeakReference<Callback>(callback);
	}
	
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		Callback mCallback = mCallbackRef.get();
		
		if (mCallback == null) {
			return false;
		}
		
		boolean flag = mCallback.onCreateActionMode(mode, menu);

		for (int i = 0; i < menu.size(); i++) {
			menu.getItem(i).setVisible(false);
		}
		
		return flag;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		Callback mCallback = mCallbackRef.get();
		
		if (mCallback == null) {
			return false;
		}
		
		
		return mCallback.onPrepareActionMode(mode, menu);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		Callback mCallback = mCallbackRef.get();
		
		if (mCallback == null) {
			return false;
		}
		
		return mCallback.onActionItemClicked(mode, item);
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		Callback mCallback = mCallbackRef.get();
		
		if (mCallback == null) {
			return;
		}
		
		mCallback.onDestroyActionMode(mode);
	}
}
