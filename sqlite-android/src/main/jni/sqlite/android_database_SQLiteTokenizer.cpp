#define LOG_TAG "SQLiteConnectionTokenizer"

#include <jni.h>
#include <JNIHelp.h>

extern "C" {
#include "fts3_html_tokenizer.h"
}

#include "android_database_SQLiteCommon.h"
#include "android_database_SQLiteTokenizer.h"

#include <sys/stat.h>
#include <android/log.h>

#define APPNAME "HTML Tokenizer"

#include <string>

namespace android {

    static void nativeRegisterTokenizer(JNIEnv *env, jclass obj, jlong connectionPtr, jstring name, jstring data) {
        const char *nameStr = env->GetStringUTFChars(name, NULL);
        const char *dataStr = NULL;

        if (data != NULL) {
            dataStr = env->GetStringUTFChars(data, NULL);
        }

        SQLiteConnection *connection = reinterpret_cast<SQLiteConnection *>(connectionPtr);
        if (connection) {

            const sqlite3_tokenizer_module *p;

            set_html_tokenizer_module(&p);

            if (p != NULL) {
                registerTokenizer(connection->db, nameStr);
            }
        }

        env->ReleaseStringUTFChars(name, nameStr);

        if (data != NULL && dataStr != NULL) {
            env->ReleaseStringUTFChars(data, dataStr);
        }
    }

    static JNINativeMethod sMethods[] =
            {
                    {"nativeRegisterTokenizer", "(JLjava/lang/String;Ljava/lang/String;)V",
                            (void *) nativeRegisterTokenizer},
            };

    int register_android_database_SQLiteConnection_Tokenizer(JNIEnv *env) {
        return jniRegisterNativeMethods(env,
                                        "org/sqlite/database/sqlite/SQLiteConnection",
                                        sMethods, NELEM(sMethods)
        );
    }

} // namespace android

