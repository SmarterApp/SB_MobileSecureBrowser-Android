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

import android.util.Log;

import com.air.mobilebrowser.AIRRecorder;
import com.air.mobilebrowser.AudioFileUtil;
import com.air.mobilebrowser.BrowserActivity;
import com.air.mobilebrowser.DeviceStatus;
import com.air.mobilebrowser.FileDecoderTask;
import com.air.mobilebrowser.FileDecoderTask.FileDecoderTaskHandler;
import com.air.mobilebrowser.FileDecryptionTask;
import com.air.mobilebrowser.FileDecryptionTask.FileDecryptionTaskHandler;
import com.air.mobilebrowser.FileEncryptionTask;
import com.air.mobilebrowser.UpdateJS;
import com.air.mobilebrowser.opusogg.OpusPlaybackListener;

/**
 * {@link JSCmd} implementation that handles starting
 * audio recording playback.
 */
public class AIRRecorderStartPlayback extends JSCmd {

	private static final String _TAG_ = "AIRRecorderStartPlayback";
	
	public static final String CMD = "cmdPlaybackAudio";
	
	public AIRRecorderStartPlayback(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(final JSONObject parameters) throws JSONException {
		final AIRRecorder recorder = AIRRecorder.getInstance();
		if (!recorder.isInit()) {
			return null;
		}
		
		if (recorder.isPlaying()) {
			recorder.stopPlayback(true);
		}
		
		final String id = super.getRequestIdentifier(parameters);
		JSONObject audioInfo = parameters.getJSONObject("audioInfo");
		final String type = audioInfo.getString("type");
		final String data = audioInfo.getString("data");
		String filename = null;
		if (audioInfo.has("filename")) {
			filename = audioInfo.getString("filename");
		}
		final String fname = filename;
		
		if (type.equals("filedata")) {
			FileDecoderTask fdt = new FileDecoderTask(new FileDecoderTaskHandler() {
				@Override
				public void onFinishedEncoding(File file) {
					recorder.startPlayback(file, new PlaybackListener(AIRRecorderStartPlayback.this.getActivity(), id));
					
					FileEncryptionTask fet = new FileEncryptionTask(new FileEncryptionTask.FileEncryptionTaskHandler() {
						
						@Override
						public void onFinishedEncrypting(File file) {}
					});
					String key = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
					fet.execute(key, AudioFileUtil.getFile(AIRRecorderStartPlayback.this.getActivity(), fname).getAbsolutePath(), file.getAbsolutePath());
					
					try {
						JSONObject toRet = new JSONObject();
						AIRRecorderStartPlayback.this.setRequestIdentifier(parameters, toRet);
						toRet.put(JSKeys.PB_STATE, "playing");
						AIRRecorderStartPlayback.this.getActivity().executeAIRMobileFunction(JSNTVCmds.NTV_ON_PLAYBACK_STATE_CHANGE, toRet.toString());
					} 
					catch (JSONException e) {}
				}
			});
			try {
				fdt.execute(AudioFileUtil.getCacheTempFile(super.getActivity(), filename, "opus").getAbsolutePath(), data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			File file = AudioFileUtil.getFile(super.getActivity(), data);
			
			if (file.exists()) {
				FileDecryptionTask fdt = new FileDecryptionTask(new FileDecryptionTaskHandler() {
					@Override
					public void onFinishedDecrypting(File file) {
						AIRRecorder recorder = AIRRecorder.getInstance();
						recorder.startPlayback(file, new PlaybackListener(AIRRecorderStartPlayback.this.getActivity(), id));
						try {
							JSONObject toRet = new JSONObject();
							toRet.put(JSKeys.REQUEST_IDENTIFIER, id);
							toRet.put(JSKeys.PB_STATE, "playing");
							AIRRecorderStartPlayback.this.getActivity().getHandler().post(new UpdateJS(AIRRecorderStartPlayback.this.getActivity(), JSNTVCmds.NTV_ON_PLAYBACK_STATE_CHANGE, toRet));
						}
						catch (JSONException e) {
							
						}
					}
				});
				File plainText;
				try {
					plainText = AudioFileUtil.getCacheTempFile(super.getActivity(), "air_audio", "opus");
				} catch (IOException e) {
					Log.e(_TAG_ , "Exception: ", e);
					throw new RuntimeException(e);
				}
				String key = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
				fdt.execute(key, file.getAbsolutePath(), plainText.getAbsolutePath());
			}
			else {
				//fire error
				JSONObject toRet = new JSONObject();
				super.setRequestIdentifier(parameters, toRet);
				toRet.put(JSKeys.PB_STATE, "error");
				return new JSCmdResponse(JSNTVCmds.NTV_ON_PLAYBACK_STATE_CHANGE, toRet);
			}
		}

		return null;
	}
		
	private class PlaybackListener implements OpusPlaybackListener {
		private String id;
		private WeakReference<BrowserActivity> activity;
		
		public PlaybackListener(BrowserActivity browserActivity, String id) {
			this.id = id;
			this.activity = new WeakReference<BrowserActivity>(browserActivity);
		}

		@Override
		public void complete(boolean stopped, String error) {
			try {
				final JSONObject toRet = new JSONObject();
				toRet.put(JSKeys.REQUEST_IDENTIFIER, id);
				if (stopped) {
					toRet.put(JSKeys.PB_STATE, "stopped");
				}
				else if (error != null) {
					toRet.put(JSKeys.PB_STATE, "error");
				}
				else {
					toRet.put(JSKeys.PB_STATE, "end");
				}
				
				final BrowserActivity bActivity = activity.get();
				if (bActivity != null) {
					bActivity.getHandler().post(new Runnable(){
						@Override
						public void run() {
							bActivity.executeAIRMobileFunction(JSNTVCmds.NTV_ON_PLAYBACK_STATE_CHANGE, toRet.toString());
							
						}
					});
				}
			} 
			catch (JSONException e) {}
		}
	}

}
 