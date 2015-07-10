/*
 * mg_opus_decoder.c
 *
 *  Created on: Apr 24, 2013
 *      Author: codyhenthrone
 */

#include <jni.h>
#include "com_air_mobilebrowser_opusogg_OpusDecoder.h"
#include "opus.h"

#ifndef NULL
#define NULL ((void*)0)
#endif

static OpusDecoder *curDecoder;
static int frame_size;
static int sample_size;

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_OpusDecoder_native_1OpusDecoderCreate(JNIEnv *env, jobject jobj, jint sampleRate, jint channels, jint outBufSize) {
	int error = 0;

	if (curDecoder != NULL) {
		return -999;
	}

	curDecoder = opus_decoder_create(sampleRate, channels, &error);
	if (error == OPUS_OK) {
        frame_size = outBufSize;
        sample_size = channels * sizeof(opus_int16);
	}

	return error;
}

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_OpusDecoder_native_1OpusDecoderDecode(JNIEnv *env, jobject jobj, jobject inBuffer, jint bytesAvail, jobject outBuffer) {
	jbyte* inputBufferJava = (*env)->GetDirectBufferAddress(env, inBuffer);
	jbyte* outputBuffer = (*env)->GetDirectBufferAddress(env, outBuffer);
	int result = opus_decode(curDecoder, inputBufferJava, bytesAvail, (opus_int16*)outputBuffer, frame_size, 0);
	if (result > 0) {
		return result * sample_size;
	}
	return result;
}

JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_OpusDecoder_native_1OpusDecoderRelease(JNIEnv *env, jobject jobj) {
	if (curDecoder != NULL) {
		opus_decoder_destroy(curDecoder);
		curDecoder = NULL;
	}
}
