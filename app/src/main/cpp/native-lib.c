#include <jni.h>
#include <string.h>
#include <stdlib.h>

JNIEXPORT void JNICALL
Java_com_example_lvglplayground_MainActivity_initNativeEngine(JNIEnv *env, jobject thiz) {
    // Inisialisasi Native LVGL Engine
}

JNIEXPORT jstring JNICALL
Java_com_example_lvglplayground_MainActivity_runScriptNative(JNIEnv *env, jobject thiz, jstring code_str) {
    const char *code = (*env)->GetStringUTFChars(env, code_str, NULL);
    
    // Proses eksekusi kode skrip
    
    (*env)->ReleaseStringUTFChars(env, code_str, code);
    return (*env)->NewStringUTF(env, "OK");
}
