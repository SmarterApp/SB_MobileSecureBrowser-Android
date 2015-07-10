/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser.jscmds;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import com.air.mobilebrowser.AudioFileUtil;
import com.air.mobilebrowser.BrowserActivity;
import com.air.mobilebrowser.DeviceStatus;
import com.air.mobilebrowser.FileDecryptionTask;
import com.air.mobilebrowser.FileDecryptionTask.FileDecryptionTaskHandler;
import com.air.mobilebrowser.FileEncoderTask;
import com.air.mobilebrowser.FileEncoderTask.FileEncoderTaskHandler;
import com.air.mobilebrowser.UpdateJS;

/**
 * {@link JSCmd} implementation that handles retrieving
 * a cached audio files on disk.
 */
public class AIRRecorderGetFile extends JSCmd {

	public static final String CMD_EXIT = "cmdRequestAudioFile";
	
	public AIRRecorderGetFile(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_EXIT, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		String fileName = parameters.getString("filename");
		File cipherFile = AudioFileUtil.getFile(super.getActivity(), fileName);
		if (cipherFile.exists()) {
			String id = super.getRequestIdentifier(parameters);
			
			FileDecryptionTask fdt = new FileDecryptionTask(new AudioEncoder(super.getActivity(), id, cipherFile));
			
			try {
				File plainTemp = AudioFileUtil.getCacheTempFile(super.getActivity(), "air_audio", "opus");
				String key = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
				fdt.execute(key, cipherFile.getAbsolutePath(), plainTemp.getAbsolutePath());
			} catch (IOException e) {}
		}
		else {
			JSONObject jsonToSend = new JSONObject();
			super.setRequestIdentifier(parameters, jsonToSend);
			JSONObject data = new JSONObject();
			data.put("base64", "");
			data.put("filename", fileName);
			
			jsonToSend.put("audioInfo", data);
			return new JSCmdResponse(JSNTVCmds.NTV_ON_AUDIO_FILE_DATA_RETRIEVED, jsonToSend);
		}
		return null;
	}

	private class AudioEncoder implements FileEncoderTaskHandler, FileDecryptionTaskHandler {
		private WeakReference<BrowserActivity> activity;
		private String id;
		private File result;
		
		public AudioEncoder(BrowserActivity browserActivity, String id, File cipherFile) {
			this.id = id;
			activity = new WeakReference<BrowserActivity>(browserActivity);
			this.result = cipherFile;
		}

		@Override
		public void onFinishedEncoding(String encodedFile) {
			if (activity.get() != null) {
				try {
					JSONObject params = new JSONObject();
					if (id != null) {
						params.put(JSKeys.REQUEST_IDENTIFIER, id);
					}

					
					JSONObject data = new JSONObject();
					data.put("base64", encodedFile);
					data.put("filename", result.getName());
					
					params.put("audioInfo", data);

					activity.get().getHandler().post(new UpdateJS(activity.get(), JSNTVCmds.NTV_ON_AUDIO_FILE_DATA_RETRIEVED, params));
				} 
				catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public void onFinishedDecrypting(File file) {
			FileEncoderTask encoder = new FileEncoderTask(this);
			encoder.execute(file.getAbsolutePath());
		}		
	}
}
