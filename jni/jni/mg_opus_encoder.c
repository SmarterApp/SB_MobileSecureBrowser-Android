/*
 * ogg_layer.c
 *
 *  Created on: Apr 23, 2013
 *      Author: codyhenthrone
 */

#include <jni.h>
#include <string.h>
#include <android/log.h>

#include "com_air_mobilebrowser_opusogg_OpusEncoder.h"
#include "opus.h"

#ifndef NULL
#define NULL ((void*)0)
#endif

static OpusEncoder *curEncoder = NULL;

static int pcm_len;
static int frame_size = 0;


JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_OpusEncoder_native_1OpusEncoderCreate(JNIEnv *env, jobject jobj, jint sampleRate, jint channels, jdouble frameDur, jint bufSize) {
	int error = 0;

	if (curEncoder != NULL) {
		return -999;
	}

	curEncoder = opus_encoder_create(sampleRate, channels, OPUS_APPLICATION_VOIP, &error);

	if (error == OPUS_OK) {
        opus_encoder_ctl(curEncoder, OPUS_SET_COMPLEXITY(5));
        opus_encoder_ctl(curEncoder, OPUS_SET_SIGNAL(OPUS_SIGNAL_VOICE));
        opus_encoder_ctl(curEncoder, OPUS_SET_LSB_DEPTH(8));

        frame_size = frameDur * sampleRate;
        pcm_len = frame_size*channels*sizeof(opus_int16);

        jclass cls = (*env)->GetObjectClass(env, jobj);
        jmethodID mid = (*env)->GetMethodID(env, cls, "setTargetSize", "(I)V");
        (*env)->CallVoidMethod(env, jobj, mid, pcm_len);
	}

	return error;
}

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_OpusEncoder_native_1OpusEncoderEncode(JNIEnv *env, jobject jobj, jobject inBB, jobject outBB, jint bytesAvail) {
	jbyte* inputBuffer = (*env)->GetDirectBufferAddress(env, inBB);
	jbyte* outputBuffer = (*env)->GetDirectBufferAddress(env, outBB);
	opus_int32 result = opus_encode(curEncoder, (opus_int16*)inputBuffer, frame_size, outputBuffer, pcm_len);
	return result;
}

/*
 *
JNIEXPORT jint JNICALL Java_com_mindgrub_samplejni_OpusEncoder_native_1OpusEncoderEncode(JNIEnv *env, jobject jobj, jobject inBB, jobject outBB, jint bytesAvail) {
	jbyte* inputBufferJava = (*env)->GetDirectBufferAddress(env, inBB);
	memcpy(bufferQueue+bufferQueueOffset, inputBufferJava, bytesAvail);
	bytesAvail += bufferQueueOffset;
	opus_int16* inputBuffer = (opus_int16*)bufferQueue;

	jbyte* outputBuffer = (*env)->GetDirectBufferAddress(env, outBB);
	opus_int32 result = 0;
	opus_int32 temp = 0;
	int count = 0;
	while (bytesAvail > pcm_len) {
		count++;
		__android_log_print(ANDROID_LOG_INFO, "OpusEncoderNative", "Hellowrold %d", count);
		temp = opus_encode(curEncoder, (opus_int16*)inputBuffer, frame_size, outputBuffer, 8192);
		if (temp > 0) {
			result += temp;
		}
		else {
			return result;
		}
		bytesAvail = bytesAvail - pcm_len;
		inputBuffer = inputBuffer + pcm_len;
	}

	if (bytesAvail > 0) {
		memcpy(bufferQueue, inputBuffer, bytesAvail);
		bufferQueueOffset = bytesAvail;
	}
	else {
		bufferQueueOffset = 0;
	}
	return result;
}
 */

JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_OpusEncoder_native_1OpusEncoderRelease(JNIEnv *env, jobject jobj) {
	if (curEncoder != NULL) {
		opus_encoder_destroy(curEncoder);
		curEncoder = NULL;
	}
}
