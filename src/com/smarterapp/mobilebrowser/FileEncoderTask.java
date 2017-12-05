//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

/**
 * AsyncTask used to base64 encode a string.
 * @author Kenny Roethel
 *
 */
public class FileEncoderTask extends AsyncTask<String, Object, String> 
{
	private static final String TAG = "FileEncoderTask";
	
	/**
	 * Implement to handle file encoding completion.
	 * @author Kenny Roethel
	 *
	 */
	public interface FileEncoderTaskHandler
	{
		public void onFinishedEncoding(String encodedFile);
	}

	/** Ref to the handler. */
	private FileEncoderTaskHandler handler;
	
	
	/** Flags to set when encoding. */
	private int encodingFlags = Base64.URL_SAFE | Base64.NO_WRAP;
	
	/**
	 * Construct a new FileEncoderTask
	 * @param handler
	 */
	public FileEncoderTask(FileEncoderTaskHandler handler)
	{
		this.handler = handler;
	}

	public void setEncodingFlags(int encodingFlagsToSet)
	{
		this.encodingFlags = encodingFlagsToSet;
	}
	
	public int getEncodingFlags()
	{
		return this.encodingFlags;
	}
	
	/**
	 * Synchronously base64 a file at a given path.
	 * @param aFilePath the path to the file which should be encoded.
	 * @return the base64 encoded file, or null if the file could not be encoded.
	 */
	public static String encodeFile(String aFilePath, int encodingFlags)
	{
		String base64Audio = null;
		
		if(aFilePath != null)
		{
			File file = new File(aFilePath);
			
			byte[] fileData = new byte[(int)file.length()];
			
			try 
			{
				new FileInputStream(file).read(fileData);
				base64Audio = Base64.encodeToString(fileData, encodingFlags);
			} 
			catch (IOException e) 
			{
				Log.e(TAG, "Error encoding", e);
				e.printStackTrace();
			}
		}
		
		return base64Audio;
	}
	
	@Override
	protected String doInBackground(String... params) 
	{
		String filePath = null;
		
		if(params != null && params.length > 0)
		{
			filePath = params[0];
		}
		return FileEncoderTask.encodeFile(filePath, this.encodingFlags);
		
	}
	
	@Override
	protected void onPostExecute(String result)
	{	
		if(handler != null)
		{
			handler.onFinishedEncoding(result);
		}
	}
}
