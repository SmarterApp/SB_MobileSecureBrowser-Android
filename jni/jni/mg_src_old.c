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

#include <samplerate.h>

#include "com_air_mobilebrowser_opusogg_SampleRateChanger.h"

SRC_STATE *src_state;
static int sample_size;

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_SampleRateChanger_native_1initSampleRateChanger
  (JNIEnv *env, jobject this, jint srcSampleRate, jint channels, jint destSampleRate) {
	int error = 0;

	if (src_state != NULL) {
		return -999;
	}

	src_state = src_new(SRC_LINEAR, channels, &error);
	sample_size = channels * sizeof(short);

	return (src_state != NULL) ? 0 : error;
}

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_SampleRateChanger_native_1processSRC
  (JNIEnv *env, jobject this, jobject inBB, jint inSampleCount, jobject outBB, jint outSampleCount, jdouble srcRatio, jint endOfInput) {

	jbyte* inputBuffer = (*env)->GetDirectBufferAddress(env, inBB);
	jbyte* outputBuffer = (*env)->GetDirectBufferAddress(env, outBB);

	float* data_in = (float*)malloc(inSampleCount*sample_size/2*sizeof(float)); //convert back to bytes, divide by sizeof(short)
	float* data_out = (float*)malloc(outSampleCount*sample_size/2*sizeof(float));

	src_short_to_float_array((short int*)inputBuffer, data_in, inSampleCount*sample_size/2);

//	__android_log_print(ANDROID_LOG_INFO, "SampleRateChanger-JNI", "Sanity: %d %d %d %f",
//			inSampleCount,
//			inSampleCount*sample_size,
//			inSampleCount*sample_size/2,
//			srcRatio);

	SRC_DATA data;

	data.data_in = data_in;
	data.input_frames = inSampleCount;
	data.data_out = data_out;
	data.output_frames = outSampleCount;
	data.src_ratio = srcRatio;
	data.end_of_input = endOfInput;

	src_set_ratio(src_state, srcRatio);
	int error = src_process(src_state, &data);

//	__android_log_print(ANDROID_LOG_INFO, "SampleRateChanger-JNI", "Results: %d %d %d", error, data.output_frames_gen, data.input_frames_used);
//	__android_log_print(ANDROID_LOG_INFO, "SampleRateChanger-JNI", "Copying %d bytes for output into %d", data.output_frames_gen*sample_size*sizeof(short int), outSampleCount*sample_size);

	if (inSampleCount != data.input_frames_used) {
		__android_log_print(ANDROID_LOG_INFO, "SampleRateChanger-JNI", "Losing frames! %d frames lost.", inSampleCount-data.input_frames_used);
	}

	src_float_to_short_array(data_out, (short int*)outputBuffer, data.output_frames_gen*sample_size);

	free(data_in);
	free(data_out);
	return (error == 0) ? data.output_frames_gen*sample_size : -error;
}

JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_SampleRateChanger_native_1release
  (JNIEnv *env, jobject this) {
	src_delete(src_state);
	src_state = NULL;
}

