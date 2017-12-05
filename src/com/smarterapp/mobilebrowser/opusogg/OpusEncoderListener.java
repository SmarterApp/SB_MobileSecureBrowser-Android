//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.opusogg;

/**
 *	Interface to implement for receiving encoding progress
 *	status updates.
 */
public interface OpusEncoderListener {

	public boolean isTimingAlert();
	public boolean isSizeAlert();
	public long getTimingAlertAmount();
	public int getSizeAlertAmount();
	
	public void progressUpdate(int mTotalBytes, long milliSeconds);
	
}
