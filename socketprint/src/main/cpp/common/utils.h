//
// Created by 1602 on 2023/5/24.
//
#pragma once

#include <string>
#include <android/log.h>
#include <stdlib.h>
#include <jni.h>

#define LOG_TAG "CommonUtil"
#define ANDROID_DEBUG

#ifdef ANDROID_DEBUG
    #define ALOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
    #define ALOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
    #define ALOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))
    #define ALOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
    #define ALOGV(...) ((void)__android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__))
#else
    #define LOGD(...);
    #define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
    #define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__))
    #define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
    #define LOGV(...);
#endif    // end of #ifdef ANDROID_DEBUG


enum PrintState {
    STATE_ERROR = -1,
    STATE_DEFAULT = 0,
    STATE_INIT,
    STATE_START,
    STATE_STOP
};

