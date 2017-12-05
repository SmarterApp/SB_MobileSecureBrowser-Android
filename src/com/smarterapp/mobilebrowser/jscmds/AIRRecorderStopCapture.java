//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.jscmds;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.smarterapp.mobilebrowser.AIRRecorder;
import com.smarterapp.mobilebrowser.AudioFileUtil;
import com.smarterapp.mobilebrowser.BrowserActivity;
import com.smarterapp.mobilebrowser.DeviceStatus;
import com.smarterapp.mobilebrowser.FileEncoderTask;
import com.smarterapp.mobilebrowser.FileEncryptionTask;
import com.smarterapp.mobilebrowser.UpdateJS;
import com.smarterapp.mobilebrowser.FileEncoderTask.FileEncoderTaskHandler;
import com.smarterapp.mobilebrowser.FileEncryptionTask.FileEncryptionTaskHandler;
import com.smarterapp.mobilebrowser.opusogg.RecordingResult;

/**
 * {@link JSCmd} implementation that handles stopping
 * audio recorder capture.
 */
public class AIRRecorderStopCapture extends JSCmd {

	private static AIRRecorderStopCapture instance;
	
	private static final String TAG = "AIRRecorderStopCapture";
	public static final String CMD_AIR_REC_STOPCAP = "cmdEndAudioCapture";
	
	public AIRRecorderStopCapture(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_AIR_REC_STOPCAP, activity, deviceStatus);
		instance = this;
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		AIRRecorder recorder = AIRRecorder.getInstance();
		if (!recorder.isInit()) {
			return null;
		}
		
		String id = super.getRequestIdentifier(parameters);
		RecordingResult result = recorder.stopCapture();
		if (result != null) {
			File encryptFile = null;
			try {
				if (result.getEndFile() != null) {
					encryptFile = AudioFileUtil.getAbsFile(super.getActivity(), result.getEndFile());
					File curPB = recorder.getCurrentPlaybackFile();
					if (curPB != null) {
						if (encryptFile.getAbsolutePath().equals(curPB.getAbsolutePath())) {
							recorder.stopPlayback(true);
						}
					}
				}
				else {
					encryptFile = AudioFileUtil.getFile(super.getActivity(), "air_audio", "opus");
				}
			} catch (IOException e) {
				Log.e(TAG, "Exception: ", e);
				throw new RuntimeException(e);
			}
			
			FileEncryptionTask encrypt = new FileEncryptionTask(new AudioEncrypt(super.getActivity(), id, result));
			String key = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			encrypt.execute(key, result.getOutputFile(), encryptFile.getAbsolutePath());
		}
		return null;
	}
	
	public static void stopCaptureHack() {
		try {
			instance.handle(new JSONObject());
		}
		catch (JSONException e) {}
	}
	
	public class AudioEncrypt implements FileEncryptionTaskHandler, FileEncoderTaskHandler {
		private WeakReference<BrowserActivity> activity;
		private String id;
		private RecordingResult result;
		private File encryptedFile;
		
		public AudioEncrypt(BrowserActivity browserActivity, String id, RecordingResult result) {
			this.id = id;
			activity = new WeakReference<BrowserActivity>(browserActivity);
			this.result = result;
		}

		@Override
		public void onFinishedEncrypting(File file) {
			this.encryptedFile = file;
			Log.i(TAG, "firing on finished encrypting");
			FileEncoderTask encoder = new FileEncoderTask(this);
			encoder.execute(result.getOutputFile());
		}
		
		@Override
		public void onFinishedEncoding(String encodedFile) {
			if (activity.get() != null) {
				try {
					JSONObject params = new JSONObject();
					params.put(JSKeys.REC_UPTYPE, JSKeys.REC_UPTYPE_END);
					if (id != null) {
						params.put(JSKeys.REQUEST_IDENTIFIER, id);
					}
					params.put(JSKeys.REC_KBYTES_REC, result.getTotalBytes() / 1024);
					params.put(JSKeys.REC_SECS_REC, result.getTotalTime() / 1000);
					
					JSONObject data = new JSONObject();
					data.put("base64", encodedFile);
					data.put("filename", encryptedFile.getName());
					data.put("qualityIndicator", result.getQuality().toString().toLowerCase(activity.get().getResources().getConfiguration().locale));
					
					params.put(JSKeys.REC_DATA, data);

					// Delete temp file, which is now encrypted as encryptedFile
					new File(result.getOutputFile()).delete();
					activity.get().getHandler().post(new UpdateJS(activity.get(), JSNTVCmds.NTV_ON_RECORDER_UPDATE, params));
				} 
				catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
