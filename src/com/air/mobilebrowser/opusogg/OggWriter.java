/********************************************************************************
* Educational Online Test Delivery System
* Copyright (c) 2015 American Institutes for Research
*
* Distributed under the AIR Open Source License, Version 1.0
* See accompanying file AIR-License-1_0.txt or at
* http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
*********************************************************************************/
package com.air.mobilebrowser.opusogg;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper class around the native OGG library. Used
 * to write ogg files to disk.
 */
public class OggWriter {
    
	static {
        System.loadLibrary("ogg-jni");
    }
    
	private ByteBuffer byteBuffer;
	private ExecutorService executor;
	
	public OggWriter(String file, int channels, int streams, int sampleRate, double frameDur, int preskip, String encoderName, int mBufSize) {
		int result = native_initOggWriter(file, channels, streams, sampleRate, frameDur, preskip, encoderName);
		if (result != 0) {
			throw new RuntimeException("Error creating OggWriter " + result);
		}
		byteBuffer = ByteBuffer.allocateDirect(mBufSize*2);
		executor = Executors.newFixedThreadPool(1);
	}
	
	public void writeOpusPacket(OpusPacket packet) {
		executor.execute(new WritePacketRunnable(packet));
	}

	public void close() {
		executor.execute(new Runnable(){
			@Override
			public void run() {
				native_closeAndFree();
			}
		});
		
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private native int native_initOggWriter(String file, int channels, int streams, int sampleRate, double frameDur, int preskip, String encoderName);
	private native void native_writePacket(ByteBuffer byteBuffer, int length);
	private native void native_closeAndFree();
	
	private class WritePacketRunnable implements Runnable {
		private OpusPacket packet;

		public WritePacketRunnable(OpusPacket pkt) {
			this.packet = pkt;
		}
		
		@Override
		public void run() {
			// byte buffer is protected by the executor service only allowing THE one thread to write at once
			byteBuffer.position(0);
			byteBuffer.put(packet.getData());
			native_writePacket(byteBuffer, packet.getData().length);
		}
	}
}
