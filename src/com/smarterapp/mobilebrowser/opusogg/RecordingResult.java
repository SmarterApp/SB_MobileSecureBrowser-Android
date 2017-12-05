//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.opusogg;

import com.smarterapp.mobilebrowser.opusogg.PCMQualityAssesser.Quality;

/**
 * Model class representing the result of an attempt
 * to record an opus encoded audio file.
 */
public class RecordingResult {
	private long mTotalTime;
	private int mTotalBytes;
	private String mOuputFile;
	private String mError;
	private Quality mQuality;
	private String mEndFile;
	
	public RecordingResult(String endFile, long totalTime, int totalBytes, String outputFile, String error) {
		this(endFile, totalTime, totalBytes, outputFile, error, Quality.UNKNOWN);
	}
	
	public RecordingResult(String endFile, long totalTime, int totalBytes, String ouputFile, String error, Quality quality) {
		super();
		this.mEndFile = endFile;
		this.mTotalTime = totalTime;
		this.mTotalBytes = totalBytes;
		this.mOuputFile = ouputFile;
		this.mError = error;
		this.mQuality = quality;
	}

	public long getTotalTime() {
		return mTotalTime;
	}

	public int getTotalBytes() {
		return mTotalBytes;
	}

	public String getOutputFile() {
		return mOuputFile;
	}

	public String getError() {
		return mError;
	}
	
	public Quality getQuality() {
		return mQuality;
	}

	/**
	 * @return the mEndFile
	 */
	public String getEndFile() {
		return mEndFile;
	}	
}
