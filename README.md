## Updating this Library

### Download latest SQLite
* Download Latest sqlite3.c and sqlite3.h in the Amalgamation file (sqlite-amalgamation-xxxx.zip) http://www.sqlite.org/download.html
* Replace existing sqlite3.c and sqlite3.h files in ./sqlite-android/src/main/jni/sqlite
* Download Latest see.c https://www.sqlite.org/see/tree?ci=trunk (authentication may be needed)
* Merge see.c with sqlite3.c file:
    * Copy see.c to ./sqlite-android/src/main/jni/sqlite (where the sqlite3.c file is located)
    * Merge the sqlite3.c with the see.c file

        cat sqlite3.c see.c > see-sqlite3.c

    * delete sqlite3.c
    * rename see-sqlite3.c to sqlite3.c
* Update the Android SQLite Java source files
    * Create a temp directory
    * fossil clone http://www.sqlite.org/android android.fossil
    * mkdir sqlite
    * cd sqlite
    * fossil open ../android.fossil
    * Compare and merge all .java files and .mk files (Use Meld tool)
        * NOTE: the following are add-ons (should be left as-is or updated separately)
            * libstreamer (required by fts3_html_tokenizer)
            * tokenizers
            * android_database_SQLiteTokenizer.cpp
            * fts3_html_tokenizer.*
            * libstemmer.h
            * stopwords.*
        * Watch for areas of the code that are added for use with tokenizers and see
            * password
            * tokenizer

### Build

* Run "Build" -> "Rebuild Project"

### Test

* Run test app and verify that all tests pass
