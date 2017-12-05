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
import java.util.Arrays;

import android.util.Log;

/**
 * Wrapper class around the native opus library. This class encodes
 * pcm streams.
 */
public class OpusEncoder {
    static {
        System.loadLibrary("ogg-jni");
    }

    public static final String TAG = "OpusEncoder";
	private ByteBuffer mInputByteBuffer;
	private ByteBuffer mOutputByteBuffer;
	private int mPCMLen;
	
	private byte[] bufferQueue;
	private int bufferQueueOffset;
	private int bufferQueueAvailBytes;
	private SampleRateChanger mSampleRateChanger;
	
	
	private OpusEncoderListener mListener;
	private int mListenerBytes;
	private long mListenerTime;
	private int mTotalBytes;
	private long mTotalTime;
	private long mStartTime;
	private long mLastTime;
    
    public OpusEncoder(int sampleRate, int channels, double frameDuration, int bufSize) {
    	int error = native_OpusEncoderCreate(sampleRate, channels, frameDuration, bufSize);
    	Log.e(TAG, "opusencodercreate code: " + error);
    	if (error != 0) {
    		throw new RuntimeException("crap on opus encoder create");
    	}
    	
    	mSampleRateChanger = new SampleRateChanger(44100, channels, sampleRate);
    	
    	mInputByteBuffer = ByteBuffer.allocateDirect(mPCMLen);
    	mOutputByteBuffer = ByteBuffer.allocateDirect(mPCMLen);
    	bufferQueue = new byte[1024*1024];
    	bufferQueueOffset = 0;
    	bufferQueueAvailBytes = 0;
    	
    	mTotalTime = 0;
    	mTotalBytes = 0;
    	mListenerBytes = 0;
    	mListenerTime = 0;
    	mLastTime = 0;
    }
    
    public void setOpusListener(OpusEncoderListener listener) {
    	mListener = listener;
    }
    
    private void setTargetSize(int pcm_len) {
    	//called from C when calling natvie_OpusEncoderCreate
    	mPCMLen = pcm_len;
    }
    
    public void release() {
    	mSampleRateChanger.release();
    	native_OpusEncoderRelease();
    }
    
	public void encodeData(OggWriter oggWriter, ByteBuffer audioInputBuffer, int bib) {
		if (mStartTime == 0) {
			mStartTime = System.currentTimeMillis();
		}
		
		mTotalTime = System.currentTimeMillis() - mStartTime;
		
		byte[] data = mSampleRateChanger.changeSampleRate(audioInputBuffer, bib);
		
		if (data == null) {
			throw new RuntimeException("No data");
		}
		
//		Log.i(TAG, "adding " + data.length + " at offset " + bufferQueueOffset);
		System.arraycopy(data, 0, bufferQueue, bufferQueueOffset, data.length);
		bufferQueueAvailBytes += data.length;
		
		int curLoopOffset = 0;
//		int rounds = 0;
		while (bufferQueueAvailBytes >= mPCMLen) {
//			Log.i(TAG, "encoding # of bytes " + mPCMLen + " in round " + rounds++ + " have avail " + bufferQueueAvailBytes);
			mInputByteBuffer.position(0);
			mInputByteBuffer.put(bufferQueue, curLoopOffset, mPCMLen);
			int encodedBytes = native_OpusEncoderEncode(mInputByteBuffer, mOutputByteBuffer, mPCMLen);
//			Log.i(TAG, "encoded size " + encodedBytes);
			oggWriter.writeOpusPacket(new OpusPacket(Arrays.copyOf(mOutputByteBuffer.array(), encodedBytes)));
			mTotalBytes += encodedBytes;
			notifyListener(encodedBytes);
			curLoopOffset += mPCMLen;
			bufferQueueAvailBytes = bufferQueueAvailBytes - mPCMLen;
//			Log.i(TAG, "bytes remaining " + bufferQueueAvailBytes + " need " + mPCMLen);
		}
		System.arraycopy(bufferQueue, curLoopOffset, bufferQueue, 0, bufferQueueAvailBytes);
		bufferQueueOffset = bufferQueueAvailBytes;
	}
    
    private void notifyListener(int encodedBytes) {
    	if (mListener != null) {
    		if (mListener.isSizeAlert()) {
    			mListenerBytes += encodedBytes;
    			if (mListenerBytes >= mListener.getSizeAlertAmount()) {
    				mListener.progressUpdate(mTotalBytes, mTotalTime);
    				mListenerBytes = mListenerBytes - mListener.getSizeAlertAmount();
    			}
    		}
    		
    		if (mListener.isTimingAlert()) {
    			if (mLastTime == 0) {
    				mLastTime = System.currentTimeMillis();
    			}
    			long curTime = System.currentTimeMillis();
    			mListenerTime += curTime - mLastTime;
    			mLastTime = curTime;
    			if (mListenerTime >= mListener.getTimingAlertAmount()) {
    				mListenerTime = mListenerTime - mListener.getTimingAlertAmount();
    				mListener.progressUpdate(mTotalBytes, mTotalTime);
    			}
    		}
    	}
	}
    
	public int getTotalBytes() {
		return mTotalBytes;
	}

	public long getTotalTime() {
		return mTotalTime;
	}

	private native int native_OpusEncoderCreate(int sampleRate, int channels, double frameDuration, int bufSize);
    private native int native_OpusEncoderEncode(ByteBuffer input, ByteBuffer output, int bytesAvail);
    private native void native_OpusEncoderRelease();
}
