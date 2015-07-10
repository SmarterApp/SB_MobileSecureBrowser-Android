/*
 * mg_ogg_reader.c
 *
 *  Created on: Apr 25, 2013
 *      Author: codyhenthrone
 */

#include <stdio.h>
#include <string.h>

#include <jni.h>
#include <android/log.h>
#include <oggz/oggz.h>

#include "com_air_mobilebrowser_opusogg_OggReader.h"
#include "opus_header.h"
#include "opus_comment.h"

#define READ_BLOCKSIZE 1024000

static OGGZ *oggzReader;
static JNIEnv *g_env;

static jobject callbackObj;
static jmethodID callbackMid;
static jmethodID shouldStopMid;
static jmethodID shouldPauseMid;
static jmethodID setHeaderInfo;

int oggz_read_callback(OGGZ * oggz, ogg_packet * op, long serialno, void * user_data) {
	jbyteArray result;

	jboolean shouldStop = (*g_env)->CallBooleanMethod(g_env, callbackObj, shouldStopMid);

	(*g_env)->CallVoidMethod(g_env, callbackObj, shouldPauseMid);

	if (!shouldStop) {
		if (op->packetno == 0) {
			OpusHeader opus_header;
			opus_header_parse(op->packet, op->bytes, &opus_header);
			/* int version, int channels, int preskip, long input_sample_rate, int gain, int channel_mapping, int nb_streams, int nb_coupled */
			(*g_env)->CallVoidMethod(g_env, callbackObj, setHeaderInfo,
					opus_header.version,
					opus_header.channels,
					opus_header.preskip,
					opus_header.input_sample_rate,
					opus_header.gain,
					opus_header.channel_mapping,
					opus_header.nb_streams,
					opus_header.nb_coupled);
		}
		else if (op->packetno > 1) {
			result = (*g_env)->NewByteArray(g_env, op->bytes);
			if (result != NULL) {

				(*g_env)->SetByteArrayRegion(g_env, result, 0, op->bytes, op->packet);
				(*g_env)->CallVoidMethod(g_env, callbackObj, callbackMid, result);
			}
		}
		return OGGZ_CONTINUE;
	}
	return OGGZ_STOP_OK;
}

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_OggReader_native_1initOggReader
  (JNIEnv *env, jobject this, jstring fileName) {
	if (oggzReader != NULL) {
		return -1;
	}

	const char *nativeFileName = (*env)->GetStringUTFChars(env, fileName, JNI_FALSE);

	oggzReader = oggz_open(nativeFileName, OGGZ_READ | OGGZ_AUTO);
	if (oggzReader != NULL) {
		oggz_seek (oggzReader, 0, SEEK_SET);

		long serial = -1;
		oggz_set_read_callback(oggzReader, serial, oggz_read_callback, NULL);
	}

	(*env)->ReleaseStringUTFChars(env, fileName, nativeFileName);

	return oggzReader != NULL ? 0 : -2;
}

JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_OggReader_native_1readOpusPackets(JNIEnv *env, jobject jobj) {
	int n;

	g_env = env;
	callbackObj = (*env)->NewGlobalRef(env, jobj);
    jclass callbackCls = (*env)->GetObjectClass(env, callbackObj);
    callbackMid = (*env)->GetMethodID(env, callbackCls, "pushOpusPacket", "([B)V");
    shouldStopMid = (*env)->GetMethodID(env, callbackCls, "shouldStop", "()Z");
    shouldPauseMid = (*env)->GetMethodID(env, callbackCls, "shouldPause", "()V");
    setHeaderInfo = (*env)->GetMethodID(env, callbackCls, "setHeaderInfo", "(IIIIIIII)V");

    while ((n = oggz_read (oggzReader, READ_BLOCKSIZE)) > 0);

	if (n == OGGZ_ERR_OUT_OF_MEMORY) {
		__android_log_print(ANDROID_LOG_ERROR, "OggReader-JNI", "out of memory");
	}
	else {
		__android_log_print(ANDROID_LOG_INFO, "OggReader-JNI", "done reading result %i", n);
	}

	(*env)->DeleteGlobalRef(env, callbackObj);
	callbackObj = NULL;
	g_env = NULL;
	callbackMid = NULL;
	shouldStopMid = NULL;
}

JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_OggReader_native_1closeAndFree
  (JNIEnv *env, jobject this) {
	oggz_close(oggzReader);
	oggzReader = NULL;
}
