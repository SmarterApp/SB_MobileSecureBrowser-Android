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
import java.util.concurrent.Semaphore;

import com.smarterapp.mobilebrowser.jscmds.AIRRecorderStopCapture;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

/**
 *	Custom runnable implementation to record/encode opus audio.
 */
public class RecorderTask implements Runnable {
	private static final String TAG = "RecorderTask";
	
	private static final int ANDROID_SAMPLE_RATE = 44100;
	private static final double OPUS_FRAME_DURATION = 0.04;
	
	private AudioRecord mAudioRecorder;
	private boolean stopRecorder = false;
	private int mBufSize;
	private OpusEncoder mOpusEncoder;
	private OggWriter mOggWriter;
	private Semaphore stopLock;
	private String mOutputFile;
	private PCMQualityAssesser mQualityAssesser;
	private String mError;
	private long mReportDurLimit;
	private int mReportSizeLimit;
	private boolean mStopHack = false;

	private String mEndFile;
	
	public RecorderTask(String outputFile, int audioSource, int channels, int sampleSize, int targetSampleRate, boolean qualityIndicator, OpusEncoderListener listener, int duration, int size, String filename) {
		int chanFormat = AudioFormat.CHANNEL_IN_STEREO;
		if (channels == 1) {
			chanFormat = AudioFormat.CHANNEL_IN_MONO;
		}
		int ssFormat = AudioFormat.ENCODING_PCM_16BIT;
		if (sampleSize == 8) {
			ssFormat = AudioFormat.ENCODING_PCM_8BIT;
		}
			
		if (duration > -1) {
			mReportDurLimit = duration * 1000;
		}
		
		if (size > -1) {
			// Convert to bytes
			mReportSizeLimit = size * 1024;
		}
		
		mEndFile = filename;
		mBufSize = AudioRecord.getMinBufferSize(ANDROID_SAMPLE_RATE, chanFormat, ssFormat);
		mAudioRecorder = new AudioRecord(audioSource, ANDROID_SAMPLE_RATE, chanFormat, ssFormat, mBufSize*2);
		mOpusEncoder = new OpusEncoder(targetSampleRate, channels, OPUS_FRAME_DURATION, mBufSize);
		mOpusEncoder.setOpusListener(listener);
		mOggWriter = new OggWriter(outputFile, channels, 1, targetSampleRate, OPUS_FRAME_DURATION, 315, "AIREncode", mBufSize);
		mOutputFile = outputFile;
		if (qualityIndicator) {
			mQualityAssesser = new PCMQualityAssesser(channels, ANDROID_SAMPLE_RATE, sampleSize);
		}
		else {
			mQualityAssesser = null;
		}
		stopLock = new Semaphore(0);
	}

	@Override
	public void run() {
		int read = 0;
		Log.i(TAG, "actually starting");		
		try {
			ByteBuffer audioInputBuffer = ByteBuffer.allocateDirect(mBufSize);

			mAudioRecorder.startRecording();
			while (!shouldStop()) {
				audioInputBuffer.position(0);
				read = mAudioRecorder.read(audioInputBuffer, mBufSize);	
				
				if (mQualityAssesser != null) {
					mQualityAssesser.assess(audioInputBuffer, mBufSize);
				}
				
				if (read >= 0) {
					mOpusEncoder.encodeData(mOggWriter, audioInputBuffer, read);
					checkLimit(mOpusEncoder.getTotalTime(), mOpusEncoder.getTotalBytes());
				}
				else {
					Log.e(TAG, "error reading data");
				}
			}
			Log.i(TAG, "actually stopping");
		}
		catch (Exception e) {
			mError = e.getMessage();
		}
		finally {
			mAudioRecorder.stop();
			mAudioRecorder.release();
			mOggWriter.close();
			stopLock.release();
			mOpusEncoder.release();
		}
	}

	private void checkLimit(long time, int size) {
		if (time > mReportDurLimit || size > mReportSizeLimit) {
			stopCaptureHack();
		}
	}

	private void stopCaptureHack() {
		if (!mStopHack) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					AIRRecorderStopCapture.stopCaptureHack();
				}
			}).start();
		}
		mStopHack  = true;
	}

	private boolean shouldStop() {
		synchronized (RecorderTask.class) {
			return stopRecorder;	
		}
	}

	public RecordingResult stopRecording(boolean forced) {
		synchronized (RecorderTask.class) {
			stopRecorder = true;	
		}
		
		try {
			stopLock.acquire();
		} catch (InterruptedException e) {}
		
		if (forced) {
			if (mError == null) {
				mError = "Forced stop";
			}
			else {
				mError += " Forced stop";
			}
		}
		
		if (mQualityAssesser != null) {
			return new RecordingResult(mEndFile, mOpusEncoder.getTotalTime(), mOpusEncoder.getTotalBytes(), mOutputFile, mError, mQualityAssesser.done());
		}
		return new RecordingResult(mEndFile, mOpusEncoder.getTotalTime(), mOpusEncoder.getTotalBytes(), mOutputFile, mError);
	}
}
