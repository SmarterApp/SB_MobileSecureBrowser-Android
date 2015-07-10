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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask used to encrypt a file.
 * @author Cody Henthorne
 *
 */
public class FileEncryptionTask extends AsyncTask<String, Void, File> 
{
	private static final String TAG = "FileEncryptionTask";
	private static final String KEYSPEC_PASS = "1aIjenM6e";
	
	/**
	 * Implement to handle file encoding completion.
	 * @author Cody Henthorne
	 *
	 */
	public interface FileEncryptionTaskHandler
	{
		public void onFinishedEncrypting(File file);
	}

	/** Ref to the handler. */
	private FileEncryptionTaskHandler handler;
	
	/**
	 * Construct a new FileEncoderTask
	 * @param handler
	 */
	public FileEncryptionTask(FileEncryptionTaskHandler handler)
	{
		this.handler = handler;
	}
	
	@Override
	protected File doInBackground(String... params) 
	{		
		if(params != null && params.length == 3)
		{
			String keyStr = params[0];
			File filePlainText = new File(params[1]);
			File fileCipherText = new File(params[2]);
			
			byte[] block = new byte[4096];
			
			FileInputStream input = null;
			CipherOutputStream output = null;
			try {
				final String KEYSPEC_ALGORITHM = "PBEWithMD5And128BitAES-CBC-OpenSSL";
				final String KEYSPEC_PROVIDER = "BC";
				byte[] localsalt = keyStr.getBytes(); 
				
				PBEKeySpec password = new PBEKeySpec(KEYSPEC_PASS.toCharArray(),localsalt, 1024,128); 
				
				SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYSPEC_ALGORITHM, KEYSPEC_PROVIDER); 
				PBEKey pbkey = (PBEKey) factory.generateSecret(password);  
				SecretKeySpec skeySpec = new SecretKeySpec(pbkey.getEncoded(), "AES");
				
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
				
				input = new FileInputStream(filePlainText);
				output = new CipherOutputStream(new FileOutputStream(fileCipherText), cipher);
				int read = input.read(block);
				while (read > 0) {
					output.write(block, 0, read);
					read = input.read(block);
				}
			} 
			catch (Exception e) {
				Log.e(TAG, "Exception: ", e);
				e.printStackTrace();
			}
			finally {
				try {
					if (input != null) input.close();
					if (output != null) output.close();
				} 
				catch (IOException e) {}
			}
			Log.e(TAG, "done encrypting");
			return fileCipherText;
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(File encryptedFile)
	{	
		Log.e(TAG, "hitting handler? " + handler);
		if(handler != null)
		{
			Log.e(TAG, "hitting handler");
			handler.onFinishedEncrypting(encryptedFile);
		}
	}
}
