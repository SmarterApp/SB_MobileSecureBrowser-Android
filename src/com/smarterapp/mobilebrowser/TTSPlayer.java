//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.annotation.SuppressLint;
import android.os.Build.*;
import android.os.Bundle;

/**
 * This class supports basic tts functionality such as play and stop
 * using standard Android TextToSpeech class APIs. No pause/resume is
 * supported, and TTS playback audio content is not written to
 * local storage for security reasons.
 * 
 * @see TextToSpeech
 * @see MediaPlayer
 */
@SuppressLint("NewApi")
public class TTSPlayer
{
	private TextToSpeech mTTSEngine;
	private WeakReference<BrowserActivity> mActivityRef;
	private String currentText = "";
	private String currentLanguage = null;
	private String currentIdentifier = null;
	private float currentPitch;
	private float currentRate;
	private float currentVolume;
	private int[] textPos;
	private int currentPos = 0;
	private boolean pauseResumeEnabled = true;

	public TTSPlayer(BrowserActivity context)
	{
		mActivityRef = new WeakReference<BrowserActivity>(context);
		mTTSEngine = new TextToSpeech(context, context);

		setUtteranceListener();
	}

	public TextToSpeech getTextToSpeech()
	{
		return mTTSEngine;
	}

	@SuppressWarnings("deprecation")
	private void setUtteranceListener()
	{
		mTTSEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() 
		{
			@Override
			public void onStart(String utteranceId) {
				
			}
			
			@Override
			public void onDone(String utteranceId) {
				
				BrowserActivity browser = mActivityRef.get();
				if(browser != null && browser.mDeviceStatus.ttsPauseResumeEnabled) {
					// retrieve the index of the completed text
					int idx = utteranceId.lastIndexOf("_");
					if (idx > -1) {
						// find out the current position of the completed text in the original spoken text
						int utteranceIdx = Integer.parseInt(utteranceId.substring(idx+1));
						int newPos = textPos[utteranceIdx];
						try {
							JSONObject jsonToSend = new JSONObject();
							jsonToSend.put("type", "word");
							jsonToSend.put("charIndex", Integer.toString(currentPos));
							jsonToSend.put("length", Integer.toString(newPos-currentPos));

							// report TTS synchronization info to JS
							browser.onTTSSynchronized(jsonToSend.toString());
							
							// if this is the last utterance in the group, update the status to idle
							if (utteranceIdx >= (textPos.length - 1)) {
								browser.mDeviceStatus.ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_IDLE;
							}
						}
						catch(JSONException e) { /* */ }
						
						currentPos = newPos;					
					}
				} else {
					browser.mDeviceStatus.ttsEngineStatus = DeviceStatus.TTS_ENGINE_STATUS_IDLE;
				}
			}
			
	        @Override
	        public void onError(String utteranceId) {
	            // There was an error.
	        }
			
		});
	}
	
	public void startPlayback(String text)
	{
		startPlayback(text, null);
	}
	
	public void startPlayback(String text, String identifier)
	{
		mTTSEngine.setLanguage(new Locale("en"));
		startPlayback(text, mTTSEngine.getLanguage().getISO3Language(), 1, 1, 1, identifier, true);
	}
	
	@SuppressLint("NewApi")
	public void startPlayback(String text, String language, float pitch, float rate, float volume, String identifier, boolean pauseResumeEnabled)
	{
		// store the current TTS settings
		currentText = text;
		currentLanguage = language;
		currentPitch = pitch;
		currentRate = rate;
		currentVolume = volume;
		currentIdentifier = identifier;
		this.pauseResumeEnabled = pauseResumeEnabled;
		
		// normalize pitch, rate, and volume to the value accepted by Android (1 is the normal value)
		pitch = pitch / 10.0f;
		rate = rate / 10.0f;
		volume = volume / 10.0f;
	
		mTTSEngine.setLanguage(new Locale(language));
		mTTSEngine.setPitch(pitch);
		mTTSEngine.setSpeechRate(rate);
		
		// test code for breaking down the text
		String[] textArr = { text };
		if (pauseResumeEnabled) {
			textArr = text.split(" ");
		}
		textPos = new int[textArr.length];
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Bundle ttsBundle = new Bundle();
			ttsBundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume);
			for (int i=0; i<textArr.length; i++) {
				String utteranceID = identifier == null ? "tts_utterance" : identifier;
				utteranceID += ("_" + Integer.toString(i));
				if (i == 0) {
					textPos[i] = textArr[i].length();
				} else {
					textPos[i] = textPos[i-1] + textArr[i].length() + 1;
				}
				mTTSEngine.speak(textArr[i], 1, ttsBundle, utteranceID);
			}
		} else {
			HashMap<String, String> myHashRender = new HashMap<String, String>();
			for (int i=0; i<textArr.length; i++) {
				String utteranceID = identifier == null ? "tts_utterance" : identifier;
				utteranceID += ("_" + Integer.toString(i));
				myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);
				if (i == 0) {
					textPos[i] = textArr[i].length();
				} else {
					textPos[i] = textPos[i-1] + textArr[i].length() + 1;
				}
				mTTSEngine.speak(textArr[i], 1, myHashRender);
			}
		}
	}
	
	protected void playback()
	{
	}

	public void pausePlayback()
	{
		if (mTTSEngine.isSpeaking() && this.pauseResumeEnabled) {
			mTTSEngine.stop();
		}
	}

	public void resumePlayback()
	{
		if (!mTTSEngine.isSpeaking() && this.pauseResumeEnabled && (currentPos < (currentText.length()-1))) {
			String remainingText = currentText;
			if (currentPos > 0) {
				remainingText = currentText.substring(currentPos + 1);
			}
			startPlayback(remainingText, currentLanguage, currentPitch, currentRate, currentVolume, currentIdentifier, true);
		}
	}

	public void stopPlayback()
	{
		if (mTTSEngine.isSpeaking()) {
			mTTSEngine.stop();
		}
	}

	public void destroy()
	{
		mTTSEngine.stop();
		mTTSEngine.shutdown();
	}
}
