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
import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;

import com.air.mobilebrowser.FileEncoderTask.FileEncoderTaskHandler;

/**
 * Wrapper around MediaRecorder that handles initializing,
 * stopping, and canceling a standard recording.
 *
 */
public class AIRAudioRecorder implements OnErrorListener, OnInfoListener
{
	public interface AudioRecorderHandler
	{
		public void onAudioRecordingComplete(boolean error, boolean cancelled, String base64Data, String identifier);
	}
	
	protected WeakReference<AudioRecorderHandler> mHandlerRef;
	protected MediaRecorder mMediaRecorder;
	protected String mIdentifier;
	protected String mFilePath;
	protected AlertDialog mAlert;
	
	/**
	 * Remove all files from the cache directory.
	 * @param context the context to use
	 */
	public static void cleanCacheDirectory(Context context)
	{
		File directory = context.getCacheDir();
		
		File[] dirContents = directory.listFiles();
		
		for(File file : dirContents)
		{
			if(file.getName().startsWith("air"))
			{
				file.delete();
			}
		}
	}
	
	/**
	 * Initialize the Recorder with the specified handler.
	 * @param handler the handler to intialize with.
	 */
	public AIRAudioRecorder(AudioRecorderHandler handler)
	{
		mMediaRecorder = new MediaRecorder();
		
		mHandlerRef = new WeakReference<AIRAudioRecorder.AudioRecorderHandler>(handler);
	}
	
	/** Reset the state of the recorder */
	public void reset()
	{
		cancelRecording();
	}
		
	/**
	 * Begin recording audio.
	 * @param context the context to use for displaying a dialog.
	 * @param duration the max duration to record for.
	 * @param identifier the request identifier 
	 * @return filePath the filepath for the generated audio file.
	 */
	public String recordAudio(Context context, int duration, String identifier)
	{
		String audioFilePath = null;
		
		if(mIdentifier == null)
		{
			mIdentifier = identifier;
			mMediaRecorder.reset();
			
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mMediaRecorder.setMaxDuration(duration*1000);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			
			mMediaRecorder.setOnInfoListener(new OnInfoListener() 
			{
				@Override
				public void onInfo(MediaRecorder mr, int what, int extra) 
				{
					if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
					{ 
						stopRecording();
					}	
					else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
					{
						//can't do anything
					}
				}
			});
			
			mMediaRecorder.setOnErrorListener(new OnErrorListener() 
			{
				@Override
				public void onError(MediaRecorder mr, int what, int extra) 
				{
					stopRecording();
				}
			});
			
			File directory = context.getCacheDir();
			File audioFile = null;
			
			try 
			{
				audioFile = File.createTempFile("air_sound", ".3gp", directory);
				audioFilePath = audioFile.getAbsolutePath();
				mFilePath = audioFilePath;
				
				mMediaRecorder.setOutputFile(audioFilePath);
				mMediaRecorder.prepare();
			} 
			catch (Exception e) {}
			
			mMediaRecorder.start();	
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle(R.string.lbl_recoring).setMessage(R.string.lbl_speak_now);
			builder.setNegativeButton(R.string.lbl_cancel, new DialogInterface.OnClickListener() 
			{	
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					cancelRecording();	
				}
			});
			builder.setPositiveButton(R.string.lbl_finished, new DialogInterface.OnClickListener()
			{	
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					stopRecording();
				}
			});
			
			mAlert = builder.show();
			
		}	
		
		return audioFilePath;
	}
	
	public void cancelRecording()
	{
		mAlert.dismiss();
		
		mMediaRecorder.stop();
		mMediaRecorder.reset();
		
		AudioRecorderHandler handler = mHandlerRef.get();
		
		if(handler != null)
		{
			handler.onAudioRecordingComplete(false, true, null, mIdentifier);
		}
		
		mIdentifier = null;
	}
	
	protected void stopRecording(boolean didError)
	{
		mAlert.dismiss();
		
		FileEncoderTask encoder = new FileEncoderTask(new FileEncoderTaskHandler() 
		{
			@Override
			public void onFinishedEncoding(String encodedFile) 
			{
				AudioRecorderHandler handler = mHandlerRef.get();
				
				if(handler != null)
				{
					handler.onAudioRecordingComplete(encodedFile == null, false, encodedFile, mIdentifier);
				}
				
				mIdentifier = null;
			}
		});
		encoder.execute(didError ? null : mFilePath);
	}
	
	public void stopRecording()
	{
		stopRecording(false);
	}

	// - MediaRecorder Handlers
	
	@Override
	public void onError(MediaRecorder mr, int what, int extra) 
	{
		stopRecording(true);
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) 
	{
		if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
		{ 
			stopRecording();
		}	
		else if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
		{
			mAlert.dismiss();
		}
	}
}
