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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

@SuppressLint("NewApi")
public class TTSChunker
{
	private int mCurrentIndex;
	
	private WeakReference<Context> mContextRef;
	
	private ArrayList<TTSChunk> chunks = new ArrayList<TTSChunk>();
	
	private TextToSpeech mTTSEngine;
	
	private MediaPlayer mMediaPlayer;
	
	private OnCompletionListener completionListener;
	
	private OnFailureListener failureListener;
	
	private OnChunkCompleteListener chunkListener;
	
	private static final String LOGTAG = "TTSCHUNKER";
	
	private enum PlaybackState
	{
		STOPPED,
		PLAYING,
		WAITING,
		PAUSED
	};
	
	private PlaybackState state = PlaybackState.STOPPED;
	
	public interface OnCompletionListener
	{
		public void onCompletion(TTSChunker chunker);
	}
	
	public interface OnFailureListener
	{
		public void onFailure(TTSChunker chunker, String errorMessage);
	}
	
	public interface OnChunkCompleteListener
	{
		public void onChunkCompleted(TTSChunker chunker, String chunk);
	}
	
	public TTSChunker(Context ctx, TextToSpeech.OnInitListener initListener)
	{
		mContextRef = new WeakReference<Context>(ctx);

		reset(initListener);
	}
	
	@SuppressWarnings("deprecation")
	private void reset(TextToSpeech.OnInitListener initListener)
	{
		Log.i(LOGTAG, "Resetting chunker");
		cleanUp();
		
		mTTSEngine = new TextToSpeech(mContextRef.get(), initListener);
		
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
		{
			mTTSEngine.setOnUtteranceCompletedListener(utteranceListener);
		}
		else
		{
			mTTSEngine.setOnUtteranceProgressListener(utteranceProgressListener);
		}

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.reset();
		mMediaPlayer.setOnCompletionListener(mediaCompleteListener);
		mMediaPlayer.setOnErrorListener(errorListener);
		mMediaPlayer.setOnPreparedListener(preparedListener);
		
		state = PlaybackState.STOPPED;
		
		mCurrentIndex = 0;
	}
	
	private void reset()
	{
		reset(null);
	}
	
	public void cleanUp()
	{
		Log.i(LOGTAG, "Cleanup chunker");
		
		if(chunks != null && chunks.size() > 0)
		{
			for(TTSChunk chunk : chunks)
			{
				chunk.cleanUp();
			}
			
			chunks.clear();
		}
		
		if(mTTSEngine != null)
		{
			mTTSEngine.stop();
			mTTSEngine.shutdown();
			mTTSEngine = null;
		}

		if(mMediaPlayer != null)
		{
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	public void startPlayback(String text, String language, float pitch, float rate, String identifier)
	{
		Log.i(LOGTAG, "Start Playback");
		pitch = pitch / 100.0f;
		rate = rate / 100.0f;
		
		if(mMediaPlayer.isPlaying())
		{
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
	
		mTTSEngine.setLanguage(new Locale(language));
		mTTSEngine.setPitch(pitch);
		mTTSEngine.setSpeechRate(rate);
		
		ArrayList<String>sentences = new ArrayList<String>();
		
		StringTokenizer tokenizer = new StringTokenizer(text, ".", true);
		
		while(tokenizer.hasMoreTokens())
		{
			sentences.add(tokenizer.nextToken());
		}
		
		Log.i(LOGTAG, "Sentences : " + sentences.size());
		
		for(String sentence : sentences)
		{
			TTSChunk chunk = new TTSChunk(sentence, identifier);
			chunks.add(chunk);
			chunk.synthesize(mTTSEngine, utteranceListener);
		}
	}
	
	public TextToSpeech getTextToSpeech()
	{
		return mTTSEngine;
	}
	
	protected void initializeMediaPlayer(String filePath)
	{
		Log.i(LOGTAG, "Init Media Player");
		try 
		{	
			Uri uri = Uri.parse("file://" + filePath);

			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(mContextRef.get(), uri);
			mMediaPlayer.prepareAsync();
		} 
		catch (Exception e) 
		{
			reset();
			
			if(failureListener != null)
			{
				failureListener.onFailure(this, "Failed to prepare media player");
			}
		}
	}
	
	public void pausePlayback()
	{
		mMediaPlayer.pause();
		
		state = PlaybackState.PAUSED;
	}

	public void resumePlayback()
	{
		if(state == PlaybackState.PAUSED)
		{
			mMediaPlayer.start();
		}
		
		state = PlaybackState.PLAYING;
	}

	public void stopPlayback()
	{
		if(mMediaPlayer.isPlaying())
		{
			mMediaPlayer.stop();
		}
		
		reset();
	}
	
	public void setOnCompletionListener(OnCompletionListener listener)
	{
		completionListener = listener;
	}
	
	public void setOnFailureListener(OnFailureListener listener)
	{
		failureListener = listener;
	}
	
	public void setChunkListener(OnChunkCompleteListener listener)
	{
		chunkListener = listener;
	}
	
	private void utteranceCompleted(String utteranceId)
	{
		Log.i(LOGTAG, "Utterance Completed");
		
		TTSChunk synthesizedChunk = null;
		
		int index = 0;
		
		for(int i = 0; i < chunks.size(); i++)
		{
			TTSChunk chunk = chunks.get(i);
			
			if(chunk.getId().equals(utteranceId))
			{
				index = i;
				synthesizedChunk = chunk;
				break;
			}
		}
		
		if(synthesizedChunk != null)
		{
			synthesizedChunk.setSynthesized(true);
			
			if((state == PlaybackState.WAITING || state == PlaybackState.STOPPED) && mCurrentIndex == index)
			{
				Log.i(LOGTAG, "State ready to play chunk");
				initializeMediaPlayer(synthesizedChunk.mChunkFilePath);
			}
			else
			{

				Log.i(LOGTAG, "State not ok to play chunk");
			}
		}
		else
		{

			Log.i(LOGTAG, "Couldn't find utterance chunk");
		}
	}
	
	private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
		@Override
		public void onStart(String utteranceId) 
		{
			Log.i(LOGTAG, "Utterance Start");
		}
		
		@Override
		public void onError(String utteranceId) 
		{
			Log.i(LOGTAG, "Utterance Error");
		}
		
		@Override
		public void onDone(String utteranceId) 
		{
			Log.i(LOGTAG, "Utterance Completed");
			utteranceCompleted(utteranceId);
		}
	};
	
	private OnUtteranceCompletedListener utteranceListener = new OnUtteranceCompletedListener() {
		@Override
		public void onUtteranceCompleted(String utteranceId)
		{
			utteranceCompleted(utteranceId);
		}
	};
	
	private OnErrorListener errorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) 
		{
			Log.i(LOGTAG, "Chunker failed");
			if(null != failureListener)
			{
				failureListener.onFailure(TTSChunker.this, "Media Player Error : " + what);
			}
			
			reset();
			return false;
		}
	};
	
	private OnPreparedListener preparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) 
		{
			Log.i(LOGTAG, "Media Player prepared");
			if(state == PlaybackState.PAUSED)//don't start if playback was paused
			{
				return;
			}
			
			TTSChunk chunk = chunks.get(mCurrentIndex);
			
			if(chunk.isSynthesized())
			{
				Log.i(LOGTAG, "Chunk synthesized, start playing");
				state = PlaybackState.PLAYING;
				
				mMediaPlayer.start();
			}
			else
			{
				Log.i(LOGTAG, "Chunk not synthesized, wait for it to synthesize");
				state = PlaybackState.WAITING;
			}
		}
	};
	
	private android.media.MediaPlayer.OnCompletionListener mediaCompleteListener = new android.media.MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) 
		{
			Log.i(LOGTAG, "Finished playing chunk");
			mCurrentIndex++;
			
			if(mCurrentIndex >= chunks.size())//all chunks played, we are finished
			{
				Log.i(LOGTAG, "No more chunks to play");
				if(completionListener != null)
				{
					completionListener.onCompletion(TTSChunker.this);
				}
				
				reset();
			}
			else
			{
				Log.i(LOGTAG, "Getting next chunk");
				
				if(chunkListener != null)
				{
					TTSChunk chunk = chunks.get(mCurrentIndex-1);
					
					chunkListener.onChunkCompleted(TTSChunker.this, chunk.getChunkString());
				}
				
				TTSChunk chunk = chunks.get(mCurrentIndex);
				
				if(chunk.isSynthesized())
				{
					Log.i(LOGTAG, "Chunk synthesized, playing chunk");
					initializeMediaPlayer(chunk.getChunkFilePath());
				}
				else
				{
					Log.i(LOGTAG, "Chunk not synthesized, waiting");
					if(state != PlaybackState.PAUSED)
					{
						state = PlaybackState.WAITING;
					}
				}
			}
		}
	};

	private class TTSChunk
	{
		private final String mId;
		private final String mChunkString;
		private final String mChunkFilePath;
		private Boolean synthesized;
		
		public TTSChunk(String chunk, String identifier)
		{
			mChunkString = chunk;
			
			mId = (identifier != null ? identifier + "-" : "") + UUID.randomUUID().toString();
			
			mChunkFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mId + ".wav";
		}
		
		public String getChunkString()
		{
			return mChunkString;
		}
		
		public String getChunkFilePath()
		{
			return mChunkFilePath;
		}
		
		public String getId()
		{
			return mId;
		}
		
		public void cleanUp()
		{
			File file = new File(mChunkFilePath);
			
			if(file != null && file.exists())
			{
				file.delete();
			}
		}
		
		public void synthesize(TextToSpeech engine, OnUtteranceCompletedListener listener)
		{
			HashMap<String, String> myHashRender = new HashMap<String, String>();
			myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mId);
			
			engine.synthesizeToFile(mChunkString, myHashRender, mChunkFilePath);
		}

		public Boolean isSynthesized() 
		{
			return synthesized;
		}

		public void setSynthesized(Boolean synthesized) 
		{
			this.synthesized = synthesized;
		}
	}
}

