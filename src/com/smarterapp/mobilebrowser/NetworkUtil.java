//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import java.net.Inet4Address;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil 
{
	public static String getIPAddress()
	{
		boolean useIPv4 = true;
		try 
		{
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) 
            {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) 
                {
                    if (!addr.isLoopbackAddress()) 
                    {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = (addr instanceof Inet4Address);
                        if (useIPv4) 
                        {
                            if (isIPv4) 
                                return sAddr;
                        } 
                        else 
                        {
                            if (!isIPv4) 
                            {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        
		return "";
	}
	
	/**
	 * Check the current status of internet connectivity.
	 * This method iterates over the available network interfaces and
	 * checks for an active connection.
	 * @return true if a connection was detected, false otherwise.
	 */
	public static boolean haveInternetConnection(Context context)
	{
		if(context != null)
		{
			ConnectivityManager connectivityMgr =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo wifiInfo = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mobile = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wimax = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
			NetworkInfo blue = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
			NetworkInfo ether = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

			boolean hasInternet = false;
			
			if (wifiInfo != null && wifiInfo.getState() == NetworkInfo.State.CONNECTED) {
				hasInternet = true;
			}
			else if (mobile != null && mobile.getState() == NetworkInfo.State.CONNECTED) {
				hasInternet = true;
			}
			else if (wimax != null && wimax.getState() == NetworkInfo.State.CONNECTED) {
				hasInternet = true;
			}
			else if (blue != null && blue.getState() == NetworkInfo.State.CONNECTED) {
				hasInternet = true;
			}
			else if (ether != null && ether.getState() == NetworkInfo.State.CONNECTED) {
				hasInternet = true;
			}
			
			return hasInternet;
		}

		return false;
	}
}
