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
 * Model class representing a single
 * opus data packet.
 */
public class OpusPacket {

	private byte[] data;
	
	public OpusPacket(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return this.data;
	}
}
