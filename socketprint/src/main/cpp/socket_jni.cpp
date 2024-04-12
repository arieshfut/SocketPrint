#include <jni.h>
#include <string>
#include <android/log.h>
#include "utils.h"
#include "socket_manager.h"


#define LOG_TAG "socket_jni"


void JstingToCstring(JNIEnv* env, std::string& cstr, jstring jstr) {
    if (nullptr != jstr) {
        const char* native_str = env->GetStringUTFChars(jstr, nullptr);
        cstr = std::string(native_str);
        env->ReleaseStringUTFChars(jstr, native_str);
    }
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_aries_print_util_GlibPrint_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    ALOGI("stringFromJNI:%s", hello.c_str());
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_aries_print_util_GlibPrint_GetPrinterName(JNIEnv *env, jobject thiz, jstring ip,
                                                   jint port) {
    std::string name = "PrinterName";
    ALOGI("GetPrinterName:%s", name.c_str());
    return env->NewStringUTF(name.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_aries_print_util_GlibPrint_PrintFile(JNIEnv *env, jobject thiz, jstring ip, jint port,
                                              jstring file_path) {
    std::string cip, fpath;
    JstingToCstring(env, cip, ip);
    JstingToCstring(env, fpath, file_path);
    ALOGI("PrintFile: ip=%s, port=%d, file path=%s", cip.c_str(), port, fpath.c_str());
    test();
}