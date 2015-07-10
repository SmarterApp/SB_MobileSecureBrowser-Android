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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 *	Receives package change broadcasts and forwards them
 *	to its handler object.
 *
 */
public class PackageReceiver extends BroadcastReceiver
{
	/** Interface to implement for receiving status changes. */
	public interface PackageUpdateHandler
	{
		public void onPackageInstalled(String packageName);
		
		public void onPackageUninstalled(String packageName);
		
		public void onPackageRestarted(String packageName);
	}
	
	protected WeakReference<PackageUpdateHandler> mHandlerRef;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		PackageUpdateHandler handler = mHandlerRef.get();
		
		if(handler != null)
		{
			String action = intent.getAction();
			String pckg = intent.getDataString();
			
			if(action.equals(Intent.ACTION_PACKAGE_ADDED))
			{
				handler.onPackageInstalled(pckg);
			}
			else if(action.equals(Intent.ACTION_PACKAGE_REMOVED))
			{
				handler.onPackageUninstalled(pckg);
			}
			else if(action.equals(Intent.ACTION_PACKAGE_RESTARTED))
			{
				handler.onPackageRestarted(pckg);
			}
		}
	}

	/** Convenience method to register the receiver, don't forget to call unregister! */
	public void register(Context context)
	{
		context.registerReceiver(this, new IntentFilter(Intent.ACTION_PACKAGE_ADDED));
	}
	
	/** Convenience method to unregister the receiver. */
	public void unregister(Context context)
	{
		context.unregisterReceiver(this);
	}
}
