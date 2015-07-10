/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.air.mobilebrowser.jscmds.JSKeys;
import com.air.mobilebrowser.jscmds.JSNTVCmds;
import com.air.mobilebrowser.opusogg.OpusEncoderListener;
import com.air.mobilebrowser.opusogg.OpusPlaybackListener;
import com.air.mobilebrowser.opusogg.PlaybackTask;
import com.air.mobilebrowser.opusogg.RecorderTask;
import com.air.mobilebrowser.opusogg.RecordingResult;

public class AIRRecorder 
{
	private static final String _TAG_ = "AIRRecorder";
	
	private static final AIRRecorder mInstance = new AIRRecorder();
	
	public static AIRRecorder getInstance() {
		return mInstance;
	}
	
	private boolean mInit;
	private PlaybackTask mPlaybackTask;
	private RecorderTask mRecorderTask;
	private AIRRecorderCapability[] mCapabilities;
	private String[] mFormats;
	private int[] mChannels;
	private int[] mSampleRates;
	private int[] mSampleSizes;
	private ReentrantLock mLock;

	private File mCurOutputFile;

	private File mCurPBFile;
	
	private AIRRecorder() {
		mLock = new ReentrantLock(true);
		mInit = false;
	}
	
	public AIRRecorderStatus initialize(final BrowserActivity activity) {
		mLock.lock();	
		if (mInit) {
			Log.i(_TAG_, "Reinitializing");
		}
		else {
			Log.i(_TAG_, "Initializing");
		}
		
		RecordingResult result = stopCapture(true);
		if (result != null) {
			if (activity != null) {
				try {
					final JSONObject params = new JSONObject();
					params.put(JSKeys.REC_UPTYPE, "ERROR");
					activity.getHandler().post(new Runnable(){
						@Override
						public void run() {
							activity.executeAIRMobileFunction(JSNTVCmds.NTV_ON_RECORDER_UPDATE, params.toString());
						}
					});
				} 
				catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		}
		stopPlayback(true);
		mInit = true;
		
		mSampleSizes = new int[] {16};
		mSampleRates = new int[] {48000, 24000, 16000, 12000, 8000};
		mChannels = new int[] {1, 2};
		mFormats = new String[] {"opus"};
		
		// There is no concept of input detection on Android, all hard coded
		mCapabilities = new AIRRecorderCapability[] {
			new AIRRecorderCapability(0, "Default Input Source",  mSampleSizes, mSampleRates, mChannels, mFormats, AudioSource.DEFAULT),
			new AIRRecorderCapability(1, "Microphone",            mSampleSizes, mSampleRates, mChannels, mFormats, AudioSource.MIC),
			new AIRRecorderCapability(2, "VoIP-Tuned Microphone", mSampleSizes, mSampleRates, mChannels, mFormats, AudioSource.VOICE_COMMUNICATION),
		};
		mLock.unlock();
		return AIRRecorderStatus.READY;
	}
	
	public boolean isInit() {
		mLock.lock();
		boolean init = mInit;
		mLock.unlock();
		return init;
	}
	
	public void startPlayback(File file, OpusPlaybackListener listener) {
		mLock.lock();
		mCurPBFile = file.getAbsoluteFile();
		mPlaybackTask = new PlaybackTask(file, listener);
		Thread playerThread = new Thread(mPlaybackTask);
		playerThread.start();
		mLock.unlock();
	}
	
	public File getCurrentPlaybackFile() {
		mLock.lock();
		File f = null; 
		if (isPlaying()) {
			if (mCurPBFile != null) {
				f = mCurPBFile.getAbsoluteFile();
			}
		}
		mLock.unlock();
		return f;
	}
	
	public void pausePlayback() {
		mLock.lock();
		if (mPlaybackTask != null) {
			mPlaybackTask.pausePlayback();
		}
		mLock.unlock();
	}
	
	public void resumePlayback() {
		mLock.lock();
		if (mPlaybackTask != null) {
			mPlaybackTask.resumePlayback();
		}
		mLock.unlock();
	}
	
	public void stopPlayback() {
		stopPlayback(false);
	}
	
	public void stopPlayback(boolean forced) {
		mLock.lock();
		if (mPlaybackTask != null) {
			mPlaybackTask.stopPlayback();
			mPlaybackTask = null;
		}
		else {
			Log.i(_TAG_, "Nothing to stop");
		}
		mLock.unlock();
	}
	
	public boolean isRecording() {
		mLock.lock();
		boolean recording = mRecorderTask != null;
		mLock.unlock();
		return recording;
	}
	
	public boolean isPlaying() {
		mLock.lock();
		boolean playing = false;
		if (mPlaybackTask != null) {
			playing = !mPlaybackTask.isStopped();
		}
		mLock.unlock();
		return playing;
	}

	public void startCapture(Context context, int capDev, int channels, int sampleRate, int sampleSize, String encodingFormat, boolean qualityIndicator, OpusEncoderListener listener, int duration, int size, String filename) throws Exception {
		mLock.lock();
		if (capDev < 0 || capDev > mCapabilities.length) {
			throw new Exception("Invalid device id: " + capDev);
		}
		else if (!checkValid(sampleRate, mSampleRates)){
			throw new Exception("Invalid sample rate: " + sampleRate);
		}
		else if (!checkValid(sampleSize, mSampleSizes)) {
			throw new Exception("Invalid sample size: " + sampleSize);
		}
		else if (!checkValid(encodingFormat, mFormats)) {
			throw new Exception("Invalid encoding format: " + encodingFormat);
		}
		
		AIRRecorderCapability device = mCapabilities[capDev];
		
		mCurOutputFile = null;
		try {
			mCurOutputFile = AudioFileUtil.getCacheTempFile(context, "air_audio", ".opus");
		} catch (IOException e) {
			Log.e(_TAG_, "Exception creating temp file", e);
			throw e;
		}
		
		mRecorderTask = new RecorderTask(mCurOutputFile.getAbsolutePath(), device.getAudioSource(), channels, sampleSize, sampleRate, qualityIndicator, listener, duration, size, filename);
		Thread recorderThread = new Thread(mRecorderTask);
		recorderThread.start();
		mLock.unlock();
	}
	
	private boolean checkValid(String value, String[] good) {
		for (String v : good) {
			if (v.equals(value)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkValid(int value, int[] good) {
		for (int v : good) {
			if (v == value) {
				return true;
			}
		}
		Log.i(_TAG_, "Error checking: " + value + " not found");
		return false;
	}
	
	public RecordingResult stopCapture() {
		return stopCapture(false);
	}
	
	public RecordingResult stopCapture(boolean forced) {
		mLock.lock();
		RecordingResult result = null;
		if (mRecorderTask != null) {
			Log.i(_TAG_, "Stopping capture");
			result = mRecorderTask.stopRecording(forced);
			mRecorderTask = null;
			mCurOutputFile = null;
		}
		else {
			Log.i(_TAG_, "No capture to stop");
		}
		mLock.unlock();
		return result;
	}
	
	public File getCurrentCaptureFile() {
		mLock.lock();
		File file = null;
		if (mCurOutputFile != null) {
			file = mCurOutputFile.getAbsoluteFile();
		}
		mLock.unlock();
		return file;
	}

	public String getError() {
		return "Error?!";
	}

	public AIRRecorderCapability[] getCapabilities() {
		mLock.lock();
		AIRRecorderCapability[] caps = Arrays.copyOf(mCapabilities, mCapabilities.length);
		mLock.unlock();
		return caps;
	}	
}
