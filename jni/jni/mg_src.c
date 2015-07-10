/*
 * mg_src.c
 *
 *  Created on: Apr 26, 2013
 *      Author: codyhenthrone
 */

#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include <jni.h>
#include <math.h>

//#include <samplerate.h>

#include "com_air_mobilebrowser_opusogg_SampleRateChanger.h"

//SRC_STATE *src_state;
static int sample_size;
static int num_channels;

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_SampleRateChanger_native_1initSampleRateChanger
  (JNIEnv *env, jobject this, jint srcSampleRate, jint channels, jint destSampleRate) {

	int error = 0;

	sample_size = channels * sizeof(short); //assumes 16bit PCM
	num_channels = channels;

	return error;
}

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_SampleRateChanger_native_1processSRC
  (JNIEnv *env, jobject this, jobject inBB, jint inSampleCount, jobject outBB, jint outSampleCount, jdouble srcRatio, jint endOfInput) {

	short* inputBuffer = (short*)(*env)->GetDirectBufferAddress(env, inBB);
	short* outputBuffer = (short*)(*env)->GetDirectBufferAddress(env, outBB);

	double inv_ratio = 1 / srcRatio;
	double idx = inv_ratio;
	int sampleIdx = 0;

	int targetSamples = srcRatio * inSampleCount;
	int outidx = 1;

	double dist;
	short sampleVal;

	outputBuffer[0] = inputBuffer[0];
	if (num_channels == 2) {
		outputBuffer[1] = inputBuffer[1];
		outidx = 2;
	}

	while (outidx < targetSamples*num_channels) {
		sampleIdx = floor(idx) * num_channels;
		dist = idx*num_channels - sampleIdx;

		sampleVal = inputBuffer[sampleIdx] + (inputBuffer[sampleIdx+num_channels] - inputBuffer[sampleIdx]) * dist;
		outputBuffer[outidx] = sampleVal;

		if (num_channels == 2) {
			sampleIdx++;
			outidx++;
			sampleVal = inputBuffer[sampleIdx] + (inputBuffer[sampleIdx+num_channels] - inputBuffer[sampleIdx]) * dist;
			outputBuffer[outidx] = sampleVal;
		}

		outidx++;
		idx += inv_ratio;
	}

	return targetSamples * sample_size;
}

JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_SampleRateChanger_native_1release
  (JNIEnv *env, jobject this) {
}
