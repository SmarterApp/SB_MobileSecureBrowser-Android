/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

/**
 * This class supports basic tts functionality such as play and stop
 * using standard Android TextToSpeech class APIs. No pause/resume is
 * supported, and TTS playback audio content is not written to
 * local storage for security reasons.
 * 
 * @see TextToSpeech
 * @see MediaPlayer
 */
public class TTSPlayer implements OnUtteranceCompletedListener
{
	private TextToSpeech mTTSEngine;
	private WeakReference<BrowserActivity> mActivityRef;

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
		mTTSEngine.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() 
		{
			@Override
			public void onUtteranceCompleted(String utteranceId) 
			{
				// initializeMediaPlayer();
				BrowserActivity activity = mActivityRef.get();

				if(activity != null)
				{
					BrowserActivity browser = mActivityRef.get();
					if(browser != null)
					{
						browser.onUtteranceCompleted("");
					}
				}
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
		startPlayback(text, mTTSEngine.getLanguage().getISO3Language(), 1, 1, identifier);
	}
	
	public void startPlayback(String text, String language, float pitch, float rate, String identifier)
	{
		pitch = pitch / 100.0f;
		rate = rate / 100.0f;
	
		mTTSEngine.setLanguage(new Locale(language));
		mTTSEngine.setPitch(pitch);
		mTTSEngine.setSpeechRate(rate);
		
		HashMap<String, String> myHashRender = new HashMap<String, String>();
		String utteranceID = identifier == null ? "tts_utterance" : identifier;
		myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);
		mTTSEngine.speak(text, 0, myHashRender);
	}
	
	protected void playback()
	{
	}

	public void pausePlayback()
	{
	}

	public void resumePlayback()
	{
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

	@Override
	public void onUtteranceCompleted(String utteranceId) 
	{
	}
}
