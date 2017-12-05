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

import android.util.Log;

/**
 * Wrapper class around the native opus library. This class decodes
 * opus encoded streams.
 */
public class OpusDecoder {
    static {
        System.loadLibrary("ogg-jni");
    }
    
    public static final String TAG = "OpusEncoder";
	private ByteBuffer mInputByteBuffer;
	private ByteBuffer mOutputByteBuffer;
    
    public OpusDecoder(int sampleRate, int channels) {
    	int bufSize = 11520; //biggest output buffer needed for an opus packet
    	
    	int error = native_OpusDecoderCreate(sampleRate, channels, bufSize);
    	Log.e(TAG, "opusdecodercreate code: " + error);
    	if (error != 0) {
    		throw new RuntimeException("crap on opus decoder create");
    	}
    	
    	mInputByteBuffer = ByteBuffer.allocateDirect(bufSize);
    	mOutputByteBuffer = ByteBuffer.allocateDirect(bufSize);
    }
    
    public ByteBuffer getInputBuffer() {
    	return mInputByteBuffer;
    }
    
    public ByteBuffer getOutputBuffer() {
    	return mOutputByteBuffer;
    }
    
	public int decodeData(int bytesAvail) {
		return native_OpusDecoderDecode(mInputByteBuffer, bytesAvail, mOutputByteBuffer);
	}
    
    public void release() {
    	native_OpusDecoderRelease();
    }

	private native int native_OpusDecoderCreate(int sampleRate, int channels, int bufSize);
	private native int native_OpusDecoderDecode(ByteBuffer payload, int payloadSize, ByteBuffer output);
	private native void native_OpusDecoderRelease();
}
