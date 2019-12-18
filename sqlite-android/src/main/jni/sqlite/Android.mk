
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# If using SEE, uncomment the following:
LOCAL_CFLAGS += -DSQLITE_HAS_CODEC

#Define HAVE_USLEEP, otherwise ALL sleep() calls take at least 1000ms
LOCAL_CFLAGS += -DHAVE_USLEEP=1

# Enable SQLite extensions.
LOCAL_CFLAGS += -DSQLITE_ENABLE_FTS5 
LOCAL_CFLAGS += -DSQLITE_ENABLE_RTREE
LOCAL_CFLAGS += -DSQLITE_ENABLE_JSON1
LOCAL_CFLAGS += -DSQLITE_ENABLE_FTS3
LOCAL_CFLAGS += -DSQLITE_ENABLE_BATCH_ATOMIC_WRITE

# This is important - it causes SQLite to use memory for temp files. Since 
# Android has no globally writable temp directory, if this is not defined the
# application throws an exception when it tries to create a temp file.
#
LOCAL_CFLAGS += -DSQLITE_TEMP_STORE=3

LOCAL_CFLAGS += -DHAVE_CONFIG_H -DKHTML_NO_EXCEPTIONS -DGKWQ_NO_JAVA
LOCAL_CFLAGS += -DNO_SUPPORT_JS_BINDING -DQT_NO_WHEELEVENT -DKHTML_NO_XBL
LOCAL_CFLAGS += -U__APPLE__
LOCAL_CFLAGS += -DHAVE_STRCHRNUL=0
LOCAL_CFLAGS += -DSQLITE_USE_URI=1
LOCAL_CFLAGS += -Wno-unused-parameter -Wno-int-to-pointer-cast
LOCAL_CFLAGS += -Wno-uninitialized -Wno-parentheses
LOCAL_CPPFLAGS += -Wno-conversion-null


# LDS added flags that don't exist in sqlite.org Android.mk file
LOCAL_CFLAGS += -DSQLITE_ENABLE_FTS3_TOKENIZER

ifeq ($(TARGET_ARCH), arm)
	LOCAL_CFLAGS += -DPACKED="__attribute__ ((packed))"
else
	LOCAL_CFLAGS += -DPACKED=""
endif

LOCAL_SRC_FILES:=                             \
	android_database_SQLiteCommon.cpp     \
	android_database_SQLiteConnection.cpp \
	android_database_SQLiteGlobal.cpp     \
	android_database_SQLiteDebug.cpp      \
	JNIHelp.cpp JniConstants.cpp

LOCAL_SRC_FILES += sqlite3.c

# ********** Tokenizer Files **********

LOCAL_SRC_FILES += android_database_SQLiteTokenizer.cpp
LOCAL_SRC_FILES += stopwords.cpp
LOCAL_SRC_FILES += fts3_html_tokenizer.c

LOCAL_SRC_FILES += libstemmer/libstemmer/libstemmer.c
LOCAL_SRC_FILES += libstemmer/runtime/api.c
LOCAL_SRC_FILES += libstemmer/runtime/utilities.c

# Languages
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_arabic.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_basque.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_basque.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_catalan.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_catalan.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_danish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_danish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_dutch.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_dutch.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_english.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_english.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_finnish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_finnish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_french.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_french.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_german.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_german.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_greek.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_hindi.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_2_hungarian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_hungarian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_indonesian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_indonesian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_irish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_irish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_italian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_italian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_lithuanian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_nepali.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_norwegian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_norwegian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_porter.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_porter.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_portuguese.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_portuguese.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_2_romanian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_romanian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_KOI8_R_russian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_russian.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_spanish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_spanish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_ISO_8859_1_swedish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_swedish.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_tamil.c
LOCAL_SRC_FILES += libstemmer/src_c/stem_UTF_8_turkish.c

LOCAL_C_INCLUDES += $(LOCAL_PATH) $(LOCAL_PATH)/libstemmer/libstemmer/
LOCAL_C_INCLUDES += $(LOCAL_PATH) $(LOCAL_PATH)/libstemmer/runtime/
LOCAL_C_INCLUDES += $(LOCAL_PATH) $(LOCAL_PATH)/libstemmer/src_c/

# ********** END Tokenizer Files **********

LOCAL_C_INCLUDES += $(LOCAL_PATH) $(LOCAL_PATH)/nativehelper/

LOCAL_MODULE:= sqliteX
LOCAL_LDLIBS += -ldl -llog

include $(BUILD_SHARED_LIBRARY)

