//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.opusogg;

import java.nio.ByteBuffer;

/**
 * Utility class to resample audio data at a different rate.
 */
public class SampleRateChanger {

	private static final String TAG = "SampleRateChanger";
	
	private int sampleSizeInBytes;
	private ByteBuffer outputByteBuffer;
	private int samplesOutBuffer;
	private double srcRatio;
	
	public SampleRateChanger(int sourceSampleRate, int channels, int destSampleRate) {
		int error = native_initSampleRateChanger(sourceSampleRate, channels, destSampleRate);
		
		if (error != 0) {
			throw new RuntimeException("Error creating SRC: " + error);
		}
		
		sampleSizeInBytes = channels * 2; // channels * sizeof(short)
		
		int outBufSize = 1024*256;
		outputByteBuffer = ByteBuffer.allocateDirect(outBufSize);
		samplesOutBuffer = outBufSize / sampleSizeInBytes;
		srcRatio = (double)destSampleRate / (double)sourceSampleRate;
	}
	
	public byte[] changeSampleRate(ByteBuffer inputBuffer, int bytesInBuffer) {
//		Log.i(TAG, "buffer avail " + bytesInBuffer);

		int samplesInBuffer = bytesInBuffer / sampleSizeInBytes;
		outputByteBuffer.position(0);
		
		int bytesRead = native_processSRC(inputBuffer, samplesInBuffer, outputByteBuffer, samplesOutBuffer, srcRatio, 0);
//		Log.i(TAG, "Processing: " + bytesRead);
		
		if (bytesRead > 0) {
			byte[] data = new byte[bytesRead];
			outputByteBuffer.get(data, 0, bytesRead);
			return data;
		}
		return null;
	}
	
	public void release() {
		native_release();
	}

	private native int native_initSampleRateChanger(int sourceSampleRate, int channels, int destSampleRate);
	private native int native_processSRC(ByteBuffer input, int numInSamples, ByteBuffer output, int numOutSamples, double srcRatio, int endOfInput);
	private native void native_release();

}
