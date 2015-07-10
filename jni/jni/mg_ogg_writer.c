/*
 * mg_ogg_writer.c
 *
 *  Created on: Apr 24, 2013
 *      Author: codyhenthrone
 */

#include <stdio.h>
#include <string.h>

#include <jni.h>
#include <android/log.h>
#include <oggz/oggz.h>

#include "com_air_mobilebrowser_opusogg_OggWriter.h"
#include "opus_header.h"
#include "opus_comment.h"
/* opus Header Stuff */
static const int kDefaultChannelCount = 1;
static const int kDefaultStreamCount = 1;
static const int kDefaultCoupledCount = 0;
static const int kDefaultMapping = 0;
static const int kDefaultSampleRate = 0;
static const int kDefaultPreskip = 315;
static const int kDefaultVersion = 1;
static const int kDefaultGain = 0;

static const char *kOpusSpecificationVersion = "1";

FILE *tempFile;
OGGZ *oggz;
long serial;
int encodedGranulePosition;
int frame_size;
long currentPacket;
ogg_packet *last_packet;

JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_OggWriter_native_1initOggWriter
  (JNIEnv *env, jobject this, jstring fileName, jint channels, jint streams, jint sampleRate, jdouble frameDur, jint preskip, jstring encoderName) {

	if (oggz != NULL) {
		return -1;
	}

    char *comments;
    int comments_length;
	const char *nativeFileName = (*env)->GetStringUTFChars(env, fileName, JNI_FALSE);
	const char *nativeEncoderName = (*env)->GetStringUTFChars(env, encoderName, JNI_FALSE);

	tempFile = fopen(nativeFileName, "wb");
	if (tempFile != NULL) {
		oggz = oggz_open_stdio(tempFile, OGGZ_WRITE);
		if (oggz != NULL) {
			serial = oggz_serialno_new(oggz);
			encodedGranulePosition = preskip;
			frame_size = frameDur * sampleRate;
			currentPacket = -1;
			last_packet = NULL;

			/* Write Header */
			OpusHeader opus_header;
			opus_header.channels = channels;
			opus_header.nb_streams = streams;
			opus_header.nb_coupled = kDefaultCoupledCount;
			opus_header.gain = kDefaultGain;
			opus_header.channel_mapping = kDefaultMapping;
			opus_header.input_sample_rate = sampleRate;
			opus_header.preskip = preskip;
			opus_header.version = kDefaultVersion;

			ogg_packet *inPacket = malloc(sizeof(ogg_packet));

			unsigned char *header_data = malloc(sizeof(char)*100);
			int packet_size = opus_header_to_packet(&opus_header, header_data, 100);

			inPacket->packet=header_data;
			inPacket->bytes=packet_size;
			inPacket->b_o_s=1;
			inPacket->e_o_s=0;
			inPacket->granulepos=0;
			inPacket->packetno=0;

			oggz_write_feed(oggz, inPacket, serial, OGGZ_FLUSH_AFTER, NULL);
			oggz_packet_destroy(inPacket);
			/* End Write Header */

			/* Write Comments */
			comment_init(&comments, &comments_length, kOpusSpecificationVersion);
			comment_add(&comments, &comments_length, "ENCODER=", "AIRMobileAndroid");

			ogg_packet op;
			op.packet=(unsigned char *)comments;
			op.bytes=comments_length;
			op.b_o_s=0;
			op.e_o_s=0;
			op.granulepos=0;
			op.packetno=1;

			oggz_write_feed(oggz, &op, serial, OGGZ_FLUSH_AFTER, NULL);

			free(comments);
			/* End Write Comments */
		}
		else {
			fclose(tempFile);
		}
	}

	(*env)->ReleaseStringUTFChars(env, fileName, nativeFileName);
	(*env)->ReleaseStringUTFChars(env, encoderName, nativeEncoderName);

	return (oggz != NULL ? 0 : -2);
}

JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_OggWriter_native_1writePacket
  (JNIEnv *env, jobject this, jobject packet, jint packetSize) {
	int error;
	jbyte* packetBuffer = (*env)->GetDirectBufferAddress(env, packet);

	encodedGranulePosition += frame_size;
	++currentPacket;

	ogg_packet *oggpacket = malloc(sizeof(ogg_packet));
	oggpacket->b_o_s = 0;
	oggpacket->e_o_s = 0;
	oggpacket->packet = malloc(packetSize);
	memcpy(oggpacket->packet, packetBuffer, packetSize);
	oggpacket->bytes = packetSize;
	oggpacket->granulepos = encodedGranulePosition;
	oggpacket->packetno = currentPacket + 2;

	if (last_packet != NULL) {
		error = oggz_write_feed(oggz, last_packet, serial, (last_packet->packetno == 2 ? OGGZ_FLUSH_BEFORE : OGGZ_FLUSH_AFTER), NULL);

		if (error != 0) {
			__android_log_print(ANDROID_LOG_ERROR, "OggWriter-JNI", "Error writing %d", error);
		}
		else {
//			__android_log_print(ANDROID_LOG_INFO, "OggWriter-JNI", "Successful write pkt %d, now running %d", last_packet->packetno, error);
			oggz_run(oggz);
		}
		free(last_packet->packet);
		free(last_packet);
	}
	last_packet = oggpacket;
}

JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_OggWriter_native_1closeAndFree
  (JNIEnv *env, jobject this) {
	last_packet->e_o_s = 1;
	int error = oggz_write_feed(oggz, last_packet, serial, (last_packet->packetno == 2 ? OGGZ_FLUSH_BEFORE : OGGZ_FLUSH_AFTER), NULL);
	if (error != 0) {
		__android_log_print(ANDROID_LOG_ERROR, "OggWriter-JNI", "Error writing %d", error);
	}
	else {
		__android_log_print(ANDROID_LOG_INFO, "OggWriter-JNI", "Successful write pkt %d, now running %d", last_packet->packetno, error);
		oggz_run(oggz);
	}
	free(last_packet->packet);
	free(last_packet);
	last_packet = NULL;

	int n = oggz_run(oggz);
	if(n < 0) {
		__android_log_print(ANDROID_LOG_ERROR, "OggWriter-JNI", "OGGZ Run Error Occurred: %i", n);
	}

	oggz_close(oggz);
	fclose(tempFile);

	oggz = NULL;
	tempFile = NULL;

	__android_log_print(ANDROID_LOG_INFO, "OggWriter-JNI", "Successful closed file");
}
