//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser.jscmds;

import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import com.smarterapp.mobilebrowser.AIRRecorder;
import com.smarterapp.mobilebrowser.BrowserActivity;
import com.smarterapp.mobilebrowser.DeviceStatus;
import com.smarterapp.mobilebrowser.UpdateJS;
import com.smarterapp.mobilebrowser.opusogg.OpusEncoderListener;
import com.smarterapp.mobilebrowser.opusogg.RecordingResult;

/**
 * {@link JSCmd} implementation that handles starting
 * audio capture.
 */
public class AIRRecorderStartCapture extends JSCmd {

	public static final String CMD_AIR_REC_STARTCAP = "cmdStartAudioCapture";
	
	public AIRRecorderStartCapture(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_AIR_REC_STARTCAP, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		AIRRecorder recorder = AIRRecorder.getInstance();
		
		if (!recorder.isInit()) {
			return null;
		}
		
		if (recorder.isRecording()) {
			RecordingResult result = recorder.stopCapture(true);
			if (result != null) {
				try {
					JSONObject params = new JSONObject();
					params.put(JSKeys.REC_UPTYPE, "ERROR");
					super.getActivity().executeAIRMobileFunction(JSNTVCmds.NTV_ON_RECORDER_UPDATE, params.toString());
				} 
				catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		String id = super.getRequestIdentifier(parameters);
		int capDev = parameters.getInt("captureDevice");
		int sampleRate = parameters.getInt("sampleRate");
		int sampleSize = parameters.getInt("sampleSize");
		int channels = parameters.getInt("channelCount");
		String encodingFormat = parameters.getString("encodingFormat");
		if (encodingFormat.toLowerCase().equals("opus")) {
			encodingFormat = "opus";	// only works if the encoding format is "opus"
		}
		boolean qualityIndicator = parameters.getBoolean("qualityIndicator");
		
		String filename = null;
		if (parameters.has("filename")) {
			filename = parameters.getString("filename");
		}
		
		JSONObject captureLimit = parameters.getJSONObject("captureLimit");
		int duration = -1;
		int size = -1;
		if (captureLimit.has("duration")) {
			duration = captureLimit.getInt("duration");
		}
		if (captureLimit.has("size")) {
			size = captureLimit.getInt("size");
		} else { // a default size limit is needed if not specified
			size = 99999;
		}
		
		OpusEncoderListener listener = null;
		if (parameters.has("progressFrequency")) {
			JSONObject progFreq = parameters.getJSONObject("progressFrequency");
			String progType = progFreq.getString("type");
			int interval = progFreq.getInt("interval");
			listener = new UpdateListener(super.getActivity(), id, progType, interval);
		}
		
		JSONObject jsonToSend = new JSONObject();
		super.setRequestIdentifier(parameters, jsonToSend);
		
		try {
			recorder.startCapture(super.getActivity(), capDev, channels, sampleRate, sampleSize, encodingFormat, qualityIndicator, listener, duration, size, filename);
			jsonToSend.put(JSKeys.REC_UPTYPE, JSKeys.REC_UPTYPE_START);
			jsonToSend.put(JSKeys.REC_KBYTES_REC, 0);
			jsonToSend.put(JSKeys.REC_SECS_REC, 0);
		} catch (Exception e) {
			jsonToSend.put(JSKeys.REC_UPTYPE, JSKeys.REC_UPTYPE_ERROR);
			jsonToSend.put(JSKeys.REC_ERROR, e.getMessage());
		}
		
		return new JSCmdResponse(JSNTVCmds.NTV_ON_RECORDER_UPDATE, jsonToSend);
	}

	private class UpdateListener implements OpusEncoderListener {
		
		private boolean mTimeBased;
		private int interval;
		private WeakReference<BrowserActivity> activity;
		private String id;
		
		public UpdateListener(BrowserActivity browserActivity, String id, String progType, int interval) {
			this.id = id;
			activity = new WeakReference<BrowserActivity>(browserActivity);
			if (progType.equals("time")) {
				mTimeBased = true;
				this.interval = interval * 1024; //Convert KB to Bytes  
			}
			else {
				mTimeBased = false;
				this.interval = interval * 1000; //Convert secs to milliseconds
			}
		}

		@Override
		public boolean isTimingAlert() {
			return mTimeBased;
		}

		@Override
		public boolean isSizeAlert() {
			return !mTimeBased;
		}

		@Override
		public long getTimingAlertAmount() {
			return interval;
		}

		@Override
		public int getSizeAlertAmount() {
			return interval;
		}

		@Override
		public void progressUpdate(int mTotalBytes, long milliSeconds) {
			if (activity.get() != null) {
				try {
					JSONObject params = new JSONObject();
					params.put(JSKeys.REC_UPTYPE, JSKeys.REC_UPTYPE_INPROGRESS);
					if (id != null) {
						params.put(JSKeys.REQUEST_IDENTIFIER, id);
					}
					params.put(JSKeys.REC_KBYTES_REC, mTotalBytes / 1024);
					params.put(JSKeys.REC_SECS_REC, milliSeconds / 1000);

					activity.get().getHandler().post(new UpdateJS(activity.get(), JSNTVCmds.NTV_ON_RECORDER_UPDATE, params));
				} 
				catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}
		}	
	}
}
