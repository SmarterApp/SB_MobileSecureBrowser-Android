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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Wrapper class around the native OGG library. Used
 * to read ogg files on disk into an AudioTrack which
 * is used to stream the audio data to a player.
 */
public class OggReader {
    static {
        System.loadLibrary("ogg-jni");
    }
	
	private static final String TAG = "OggReader";
	private AudioTrack mAudioTrack;
	private OpusDecoder mOpusDecoder;
	private ByteBuffer mOpusDecoderInBuffer;
	private byte[] mOpusDecoderOutBuffer;
	
	private boolean mShouldStop = false;
	private int mChannels;
	private int mSampleRate;
	private boolean mShouldPause;
	private String mError;
	
	public OggReader(String file) {		
		mShouldStop = false;
		mShouldPause = false;
		int result = native_initOggReader(file);
		if (result != 0) {
			throw new RuntimeException("Error creating OggReader " + result);
		}
	}
		
	/* Called from native code */
	private synchronized boolean shouldStop() {
		return mShouldStop;
	}
	
	/* Called from native code */
	private synchronized void shouldPause() {
		if (mShouldPause) {
			try {
				Log.i(TAG, "stopping");
				this.wait();
				Log.i(TAG, "resuming...");
			} 
			catch (InterruptedException e) {}
		}
	}
	
	public synchronized void resumePlayback() {
		if (mShouldPause) {
			mShouldPause = false;
			mAudioTrack.play();
			this.notifyAll();
		}
	}
	
	public synchronized void pausePlayback() {
		if (!mShouldPause) {
			mShouldPause = true;
			mAudioTrack.pause();
		}
	}

	public synchronized void stopPlayback() {
		mShouldPause = false;
		mShouldStop = true;
		this.notifyAll();
	}
	
	public String readOpusPackets() {
		try {
			native_readOpusPackets();
		}
		catch (Exception e) {
			mError = e.getMessage();
		}
		finally {
			mOpusDecoder.release();
			mAudioTrack.stop();
			mAudioTrack.release();
		}
		return mError;
	}
	
	/* Called from native code */
	private void pushOpusPacket(byte[] data) {
		mOpusDecoderInBuffer.position(0);
		mOpusDecoderInBuffer.put(data);
		int decodedSize = mOpusDecoder.decodeData(data.length);
//		Log.i(TAG, "decoded size: " + decodedSize);
		if (decodedSize > 0) {
			mAudioTrack.write(mOpusDecoderOutBuffer, 0, decodedSize);
		}	
	}
	
	/* Called from native code */
	private void setHeaderInfo(int version, int channels, int preskip, int input_sample_rate, int gain, int channel_mapping, int nb_streams, int nb_coupled) {
		mChannels = channels;
		mSampleRate = input_sample_rate;
		
		int outFormat = (mChannels == 1) ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
		int bufSize = AudioTrack.getMinBufferSize(mSampleRate, outFormat, AudioFormat.ENCODING_PCM_16BIT) * 2;
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, outFormat, AudioFormat.ENCODING_PCM_16BIT, bufSize, AudioTrack.MODE_STREAM);

		mOpusDecoder = new OpusDecoder(mSampleRate, mChannels);
		mOpusDecoderInBuffer = mOpusDecoder.getInputBuffer();
		mOpusDecoderOutBuffer = mOpusDecoder.getOutputBuffer().array();
		
		mAudioTrack.play();
	}
	
	public void release() {
		native_closeAndFree();
	}

	private native int native_initOggReader(String file);
	private native void native_readOpusPackets();
	private native void native_closeAndFree();

}
