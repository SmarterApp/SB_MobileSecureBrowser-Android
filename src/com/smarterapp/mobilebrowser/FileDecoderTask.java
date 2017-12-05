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
import java.io.FileOutputStream;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

/**
 * AsyncTask used to decode a base64 encoded file.
 * @author Cody Henthorne
 *
 */
public class FileDecoderTask extends AsyncTask<String, Void, File> 
{
	private static final String TAG = "FileDecoderTask";
	
	/**
	 * Implement to handle file decoding completion.
	 * @author Cody Henthorne
	 *
	 */
	public interface FileDecoderTaskHandler
	{
		public void onFinishedEncoding(File file);
	}

	/** Ref to the handler. */
	private FileDecoderTaskHandler handler;
	
	
	/** Flags to set when decoding. */
	private int encodingFlags = Base64.NO_WRAP | Base64.URL_SAFE;
	
	/**
	 * Construct a new FileDecoderTask
	 * @param handler
	 */
	public FileDecoderTask(FileDecoderTaskHandler handler)
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
	 * Synchronously base64 decode into a file at a given path.
	 * @param filePath file to store decoded data
	 * @param data the data to decoded
	 * @param encodingFlags flags used for decoding
	 */
	private void decodeFile(File filePath, String data, int encodingFlags)
	{
		if(filePath != null)
		{			
			try 
			{
				byte[] decodedData = Base64.decode(data.getBytes(), encodingFlags);
				FileOutputStream fos = new FileOutputStream(filePath);
				fos.write(decodedData);
				fos.close();
			} 
			catch (Exception e) 
			{
				Log.e(TAG, "Error decoding", e);
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected File doInBackground(String... params) 
	{
		File filePath = null;
		String data = null;
		
		if(params != null && params.length == 2)
		{
			filePath = new File(params[0]);
			data = params[1];
		}
		
		decodeFile(filePath, data, encodingFlags);
		return filePath;
	}
	
	@Override
	protected void onPostExecute(File result)
	{	
		if(handler != null)
		{
			handler.onFinishedEncoding(result);
		}
	}
}
