/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser;

/**
 *	Represents the various possible states
 *	of a recorder.
 */
public enum AIRRecorderStatus {
	UNINITIALIZED,
	INITIALIZING,
	IDLE,
	ACTIVE,
	PLAYING,
	STOPPING,
	ERROR, READY,
	
}
