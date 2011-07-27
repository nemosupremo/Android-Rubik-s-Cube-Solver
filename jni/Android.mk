LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := colordecoder
LOCAL_SRC_FILES := cubex.cpp solver.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)