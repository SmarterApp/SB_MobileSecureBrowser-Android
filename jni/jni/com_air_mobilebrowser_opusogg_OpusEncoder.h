/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_air_mobilebrowser_opusogg_OpusEncoder */

#ifndef _Included_com_air_mobilebrowser_opusogg_OpusEncoder
#define _Included_com_air_mobilebrowser_opusogg_OpusEncoder
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_air_mobilebrowser_opusogg_OpusEncoder
 * Method:    native_OpusEncoderCreate
 * Signature: (IIDI)I
 */
JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_OpusEncoder_native_1OpusEncoderCreate
  (JNIEnv *, jobject, jint, jint, jdouble, jint);

/*
 * Class:     com_air_mobilebrowser_opusogg_OpusEncoder
 * Method:    native_OpusEncoderEncode
 * Signature: (Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_air_mobilebrowser_opusogg_OpusEncoder_native_1OpusEncoderEncode
  (JNIEnv *, jobject, jobject, jobject, jint);

/*
 * Class:     com_air_mobilebrowser_opusogg_OpusEncoder
 * Method:    native_OpusEncoderRelease
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_air_mobilebrowser_opusogg_OpusEncoder_native_1OpusEncoderRelease
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif