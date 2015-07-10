/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser.opusogg;

import java.nio.ByteBuffer;

/**
 * Utility class used to asses the quality of a recording
 *	The algorithm used works by determining if the samples 
 *	provided in the audio file have a percentage of samples (eg 25%)
 *	that exceed the volume amplitude of the first half second
 *	of audio.
 */
public class PCMQualityAssesser {
	
	public static enum Quality {
		GOOD,
		POOR,
		UNKNOWN,
	}
	
	private int mNumChannels;
	private int mSampleSizeInBytes;
	private short[] mBuffer;
	private int mBufferIdx;
	private int mSamplesPerHalfSecond;

	private int mNoiseFloor;
	private int mNumChunksOverNoiseFloor;
	private int mNumNoiseChunks;
	private int mPCMBits;

	
	public PCMQualityAssesser(int channels, int sampleRate, int pcmBits) {
		this.mPCMBits = pcmBits;
		this.mNumChannels = channels;

		this.mSampleSizeInBytes = mNumChannels * (pcmBits >> 3); // pcmBits / 8
		this.mSamplesPerHalfSecond = sampleRate * mNumChannels / 2;
		
		mBuffer = new short[sampleRate * (mSampleSizeInBytes >> 1) * 2];
		mBufferIdx = 0;
		mNoiseFloor = -1;
		
		mNumChunksOverNoiseFloor = 0;
		mNumNoiseChunks = 0;
	}

	public void assess(ByteBuffer audioInputBuffer, int mBufSize) {
		byte[] inputBuffer = audioInputBuffer.array();
		short val;
		for (int i = 0; i < mBufSize;) {
			if (mPCMBits == 8) {
				val = inputBuffer[i++];
			}
			else {
				val = (short)((inputBuffer[i+1] << 8) + inputBuffer[i]);
				i += 2;
			}
			mBuffer[mBufferIdx++] = val;
		}

		if (mBufferIdx >= mSamplesPerHalfSecond) {	
			short peak = getPeakSignalLevel(mNumChannels);
			
			if (mNoiseFloor == -1) {
				mNoiseFloor = peak;
			}
			else {
				if (peak > mNoiseFloor*2) {
					mNumChunksOverNoiseFloor++;
				}
				else {
					mNumNoiseChunks++;
				}
			}
			
			// Move down
			System.arraycopy(mBuffer, mSamplesPerHalfSecond, mBuffer, 0, mBufferIdx - mSamplesPerHalfSecond);
			mBufferIdx = mBufferIdx - mSamplesPerHalfSecond;
		}
	}
	
	public Quality done() {
		if((mNumChunksOverNoiseFloor*1.0/(mNumNoiseChunks+mNumChunksOverNoiseFloor) > 0.25) ||
				mNumChunksOverNoiseFloor > 30) {
			return Quality.GOOD;
		}
		else {
			return Quality.POOR;
		}
	}
	
	/**
	 * Just use left channel/mono
	 * @param channels
	 * @return
	 */
	private short getPeakSignalLevel(int channels) {
		short peak = 0;
		
		short test;
		for (int i = 0; i < mSamplesPerHalfSecond; i+=channels) {
			test = (short)Math.abs(mBuffer[i]);
			peak = test > peak ? test : peak;
		}
		
		return peak;
	}

}
