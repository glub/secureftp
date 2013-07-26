#include <jni.h>
#include "com_glub_jni_UID.h"
#include <sys/types.h>
#include <unistd.h>

JNIEXPORT jint JNICALL 
Java_com_glub_jni_UID_setuid (JNIEnv *jnienv, jclass j, jint uid) {
  return ((jint)setuid((uid_t)uid));
}

JNIEXPORT jint JNICALL 
Java_com_glub_jni_UID_seteuid (JNIEnv *jnienv, jclass j, jint uid) {
  return ((jint)seteuid((uid_t)uid));
}

JNIEXPORT jint JNICALL 
Java_com_glub_jni_UID_setgid (JNIEnv *jnienv, jclass j, jint gid) {
  return ((jint)setgid((uid_t)gid));
}

JNIEXPORT jint JNICALL 
Java_com_glub_jni_UID_setegid (JNIEnv *jnienv, jclass j, jint gid) {
  return ((jint)setegid((uid_t)gid));
}

