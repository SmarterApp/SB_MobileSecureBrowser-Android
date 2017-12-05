//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.smarterapp.mobilebrowser.ExecShell.SHELL_CMD;

import android.util.Log;

/**
 * Utility class that checks if the device is rooted. *Note
 * that the methods used to detect a rooted device are not
 * 100% accurate and will not necessarily work on all rooted
 * devices. 
 */
public class RootUtility 
{
	/**
	 * Check if a rooted device is detected.
	 * @return true if it was detected that the device is rooted, false otherwise.
	 */
	public static boolean isDeviceRooted() 
	{
		if (checkRootMethod1()){return true;}
		if (checkRootMethod2()){return true;}
		if (checkRootMethod3()){return true;}
		return false;
	}

	public static boolean checkRootMethod1()
	{
		String buildTags = android.os.Build.TAGS;

		if (buildTags != null && buildTags.contains("test-keys")) 
		{
			return true;
		}
		return false;
	}

	public static boolean checkRootMethod2()
	{
		try 
		{
			File file = new File("/system/app/Superuser.apk");
			if (file.exists()) 
			{
				return true;
			}
		} catch (Exception e) { }

		return false;
	}

	public static boolean checkRootMethod3() 
	{
		if (new ExecShell().executeCommand(SHELL_CMD.check_su_binary) != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}

 class ExecShell 
 {
	private static String LOG_TAG = ExecShell.class.getName();

	public static enum SHELL_CMD 
	{
		check_su_binary(new String[] {"/system/xbin/which","su"}),
		;

		String[] command;

		SHELL_CMD(String[] command){
			this.command = command;
		}
	}

	public ArrayList<String> executeCommand(SHELL_CMD shellCmd)
	{
		String line = null;
		ArrayList<String> fullResponse = new ArrayList<String>();
		Process localProcess = null;

		try 
		{
			localProcess = Runtime.getRuntime().exec(shellCmd.command);
		} 
		catch (Exception e) 
		{
			return null;
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));

		try {
			while ((line = in.readLine()) != null) {
				Log.d(LOG_TAG, "--> Line received: " + line);
				fullResponse.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.d(LOG_TAG, "--> Full response was: " + fullResponse);

		return fullResponse;
	}

}
