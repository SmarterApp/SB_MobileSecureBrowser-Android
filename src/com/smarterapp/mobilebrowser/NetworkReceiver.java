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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Receives network status change broadcasts and forwards them
 * to its handler object.
 */
public class NetworkReceiver extends BroadcastReceiver 
{
	/** Interface to implement for receiving status changes. */
	public interface NetworkStatusHandler
	{
		/** Called when the broadcast is received. */
		public void onNetworkStatusChanged(NetworkInfo netInfo);
	}
	
	/** Reference to the handler object. */
	protected WeakReference<NetworkStatusHandler> handlerRef;

	/** Construct a new receiver with a specified handler. */
	public NetworkReceiver(NetworkStatusHandler handler)
	{
		handlerRef = new WeakReference<NetworkReceiver.NetworkStatusHandler>(handler);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{	
		NetworkStatusHandler handler = handlerRef.get();
		
		if(handler != null)
		{
			@SuppressWarnings("deprecation")
			NetworkInfo netInfo = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
			
			handler.onNetworkStatusChanged(netInfo);
		}
	}
	
	/** Convenience method to register the receiver, don't forget to call unregister! */
	public void register(Context context)
	{
		context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	/** Convenience method to unregister the reciever. */
	public void unregister(Context context)
	{
		context.unregisterReceiver(this);
	}
}