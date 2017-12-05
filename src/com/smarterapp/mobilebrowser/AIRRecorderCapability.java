//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

/**
 * Model class used to store data about 
 * the devices recording capabilities.
 */
public class AIRRecorderCapability {

	private int id;
	private String description;
	private int[] sampleSizes;
	private int[] channels;
	private String[] formats;
	private int audioSource;
	private int[] sampleRates;

	public AIRRecorderCapability(int id, String description, int[] sampleSizes, int[] sampleRates, int[] channels, String[] formats, int audioSource) {
		this.id = id;
		this.description = description;
		this.sampleSizes = sampleSizes;
		this.sampleRates = sampleRates;
		this.channels = channels;
		this.formats = formats;
		this.audioSource = audioSource;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public int[] getSampleSizes() {
		return sampleSizes;
	}
	
	public int[] getSampleRates() {
		return sampleRates;
	}

	public int[] getChannels() {
		return channels;
	}

	public String[] getFormats() {
		return formats;
	}

	public int getAudioSource() {
		return audioSource;
	}

}
