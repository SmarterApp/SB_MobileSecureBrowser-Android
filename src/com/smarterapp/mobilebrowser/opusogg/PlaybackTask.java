//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.opusogg;

import java.io.File;

/**
 *	Custom Runnable that manages an {@link OGGReader} to
 *	playback an opus encoded audio file. 
 */
public class PlaybackTask implements Runnable {

	private OggReader mOggReader;
	private OpusPlaybackListener pbListener;
	private boolean stopped = false;
	private File outputFile;

	public PlaybackTask(File outputFile, OpusPlaybackListener pbListener) {
		this.outputFile = outputFile;
		this.pbListener = pbListener;
	}
	
	@Override
	public void run() {
		stopped = false;
		mOggReader = new OggReader(outputFile.getAbsolutePath());
		
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		String error = null;
		try {
			error = mOggReader.readOpusPackets();
		}
		catch (Exception e) {
			error = e.getMessage();
		}
		finally {
			mOggReader.release();
			if (pbListener != null) {
				pbListener.complete(stopped, error);
			}
			stopped = true;
		}
	}

	public void stopPlayback() {
		if (!stopped) {
			mOggReader.stopPlayback();
		}
		stopped = true;
	}
	
	public void pausePlayback() {
		mOggReader.pausePlayback();
	}
	
	public void resumePlayback() {
		mOggReader.resumePlayback();
	}

	public boolean isStopped() {
		return stopped;
	}
}
