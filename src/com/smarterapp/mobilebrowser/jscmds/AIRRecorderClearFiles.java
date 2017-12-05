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
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smarterapp.mobilebrowser.AIRRecorder;
import com.smarterapp.mobilebrowser.AudioFileUtil;
import com.smarterapp.mobilebrowser.BrowserActivity;
import com.smarterapp.mobilebrowser.DeviceStatus;

/**
 * {@link JSCmd} implementation that handles clearing the 
 * cached audio files on disk.
 */
public class AIRRecorderClearFiles extends JSCmd {

	public static final String CMD_EXIT = "cmdRequestClearAudioCache";
	
	public AIRRecorderClearFiles(BrowserActivity activity, DeviceStatus deviceStatus) {
		super(CMD_EXIT, activity, deviceStatus);
	}

	@Override
	public JSCmdResponse handle(JSONObject parameters) throws JSONException {
		AIRRecorder recorder = AIRRecorder.getInstance();
		
		HashSet<String> filesToSkip = new HashSet<String>();
		if (recorder.isPlaying()) {
			File pbFile = recorder.getCurrentPlaybackFile();
			if (pbFile != null) {
				filesToSkip.add(pbFile.getAbsolutePath());
			}
		}
		AudioFileUtil.cleanDirectory(super.getActivity(), filesToSkip);
		
		File[] files = AudioFileUtil.getDirectoyContents(super.getActivity());
		File skip = null;
		if (recorder.isRecording()) {
			skip = recorder.getCurrentCaptureFile();
		}
		
		JSONArray array = new JSONArray();
		for (File f : files) {
			if (skip == null || !f.equals(skip)) {
				array.put(f.getName());
			}
		}
		
		JSONObject jsonToSend = new JSONObject();
		super.setRequestIdentifier(parameters, jsonToSend);
		jsonToSend.put("files", array);
		
		return new JSCmdResponse(JSNTVCmds.NTV_ON_AUDIO_FILE_LIST_RETRIEVED, jsonToSend);
	}

}
