package org.sqlite.app.customsqlite

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sqlite.database.enums.Tokenizer
import org.sqlite.database.sqlite.SQLiteDatabase
import org.sqlite.database.sqlite.SQLiteDatabaseCorruptException
import org.sqlite.database.sqlite.SQLiteOpenHelper
import java.io.FileInputStream
import java.util.Arrays

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val databaseFile by lazy { application.getDatabasePath("test.db") }

    private lateinit var logTextView: TextView          /* Text view widget */
    private lateinit var myProgress: ProgressBar
    private lateinit var myButton: Button
    private var myNTest: Int = 0            /* Number of tests attempted */
    private var myNErr: Int = 0             /* Number of tests failed */

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        logTextView = findViewById<View>(R.id.tv_widget) as TextView
        myProgress = findViewById<View>(R.id.progress) as ProgressBar
        myButton = findViewById<View>(R.id.btnRun) as Button

        myButton.setOnClickListener { runTheTests() }
    }

    private fun runTheTests() = launch {
        System.loadLibrary("sqliteX") // loads the custom sqlite library

        databaseFile.mkdirs()

        logTextView.text = ""
        myNErr = 0
        myNTest = 0

        myProgress.visibility = View.VISIBLE

        withContext(Dispatchers.IO) {
            try {
                reportVersion()
                csrTest1()
                csrTest2()
                threadTest1()
                threadTest2()
                seeTest1()
                seeTest2()
                ftsTest1()
                ftsTest2()
                ftsTest3()
                suppCharTest1()
                suppCharTest2()
                stemmerTest1()
                stemmerTest2()
            } catch (e: Exception) {
                appendString("Exception: " + e.toString() + "\n")
            }
        }

        appendString("\n$myNErr errors from $myNTest tests\n")
        myProgress.visibility = View.GONE
    }

    private suspend fun reportVersion() {
        SQLiteDatabase.openOrCreateDatabase(":memory:", null).use { db ->
            val statement = db.compileStatement("SELECT sqlite_version()")
            appendString("SQLite version ${statement.simpleQueryForString()}\n\n")
        }
    }

    private suspend fun testWarning(name: String, warning: String) {
        appendString("WARNING:$name: $warning\n")
    }

    private suspend fun testResult(name: String, res: String, expected: String) {
        appendString("$name... ")
        myNTest++

        if (res == expected) {
            appendString("ok\n")
        } else {
            myNErr++
            appendString("FAILED\n")
            appendString("   res=     \"$res\"\n")
            appendString("   expected=\"$expected\"\n")
        }
    }

    /**
     * Test if the database at DB_PATH is encrypted or not. The db
     * is assumed to be encrypted if the first 6 bytes are anything
     * other than "SQLite".
     *
     * If the test reveals that the db is encrypted, return the string
     * "encrypted". Otherwise, "unencrypted".
     */
    @Throws(Exception::class)
    private fun dbIsEncrypted(): String {
        FileInputStream(databaseFile).use {
            val buffer = ByteArray(6)
            it.read(buffer, 0, 6)

            var resultText = "encrypted"
            if (Arrays.equals(buffer, "SQLite".toByteArray())) {
                resultText = "unencrypted"
            }
            return resultText
        }
    }

    /**
     * Test that a database connection may be accessed from a second thread.
     */
    private suspend fun threadTest1() = coroutineScope {
        SQLiteDatabase.deleteDatabase(databaseFile)
        val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)

        val db_path2 = databaseFile.toString() + "2"

        db.execSQL("CREATE TABLE t1(x, y)")
        db.execSQL("INSERT INTO t1 VALUES (1, 2), (3, 4)")

        async(Dispatchers.Default) {
            val st = db.compileStatement("SELECT sum(x+y) FROM t1")
            val res = st.simpleQueryForString()
            testResult("thread_test_1", res, "10")
        }
    }

    /**
     * Test that a database connection may be accessed from a second thread.
     */
    private suspend fun threadTest2() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)

        db.execSQL("CREATE TABLE t1(x, y)")
        db.execSQL("INSERT INTO t1 VALUES (1, 2), (3, 4)")

        db.enableWriteAheadLogging()
        db.beginTransactionNonExclusive()
        db.execSQL("INSERT INTO t1 VALUES (5, 6)")

        val thread = Thread(Runnable {
            val st = db.compileStatement("SELECT sum(x+y) FROM t1")
            val res = st.simpleQueryForString()
        })

        thread.start()
        var res = "concurrent"

        var i = 0
        while (i < 20 && thread.isAlive) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                // nothing
            }

            i++
        }

        if (thread.isAlive) {
            res = "blocked"
        }

        db.endTransaction()
        try {
            thread.join()
        } catch (e: InterruptedException) {
            // nothing
        }

        if (SQLiteDatabase.hasCodec()) {
            testResult("thread_test_2", res, "blocked")
        } else {
            testResult("thread_test_2", res, "concurrent")
        }
    }

    /*
    ** Use a Cursor to loop through the results of a SELECT query.
    */
    @Throws(Exception::class)
    private suspend fun csrTest2() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        var res = ""
        var expect = ""
        var i: Int
        var nRow = 0

        db.execSQL("CREATE TABLE t1(x)")
        db.execSQL("BEGIN")
        i = 0
        while (i < 1000) {
            db.execSQL("INSERT INTO t1 VALUES ('one'), ('two'), ('three')")
            expect += ".one.two.three"
            i++
        }
        db.execSQL("COMMIT")
        var c: Cursor? = db.rawQuery("SELECT x FROM t1", null)
        if (c != null) {
            var bRes: Boolean
            bRes = c.moveToFirst()
            while (bRes) {
                val x = c.getString(0)
                res = "$res.$x"
                bRes = c.moveToNext()
            }
        } else {
            testWarning("csr_test_1", "c==NULL")
        }
        testResult("csr_test_2.1", res, expect)

        db.execSQL("BEGIN")
        i = 0
        while (i < 1000) {
            db.execSQL("INSERT INTO t1 VALUES (X'123456'), (X'789ABC'), (X'DEF012')")
            db.execSQL("INSERT INTO t1 VALUES (45), (46), (47)")
            db.execSQL("INSERT INTO t1 VALUES (8.1), (8.2), (8.3)")
            db.execSQL("INSERT INTO t1 VALUES (NULL), (NULL), (NULL)")
            i++
        }
        db.execSQL("COMMIT")

        c = db.rawQuery("SELECT x FROM t1", null)
        if (c != null) {
            var bRes: Boolean
            bRes = c.moveToFirst()
            while (bRes) {
                nRow++
                bRes = c.moveToNext()
            }
        } else {
            testWarning("csr_test_1", "c==NULL")
        }

        testResult("csr_test_2.2", "" + nRow, "15000")

        db.close()
    }

    @Throws(Exception::class)
    private suspend fun csrTest1() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        var res = ""

        db.execSQL("CREATE TABLE t1(x)")
        db.execSQL("INSERT INTO t1 VALUES ('one'), ('two'), ('three')")

        val c = db.rawQuery("SELECT x FROM t1", null)
        if (c != null) {
            var bRes: Boolean
            bRes = c.moveToFirst()
            while (bRes) {
                val x = c.getString(0)
                res = "$res.$x"
                bRes = c.moveToNext()
            }
        } else {
            testWarning("csr_test_1", "c==NULL")
        }
        testResult("csr_test_1.1", res, ".one.two.three")

        db.close()
        testResult("csr_test_1.2", dbIsEncrypted(), "unencrypted")
    }

    private fun readStringFromTable1(db: SQLiteDatabase): String {
        var res = ""

        val cursor = db.rawQuery("SELECT x FROM t1", null)
        var bRes: Boolean
        bRes = cursor.moveToFirst()
        while (bRes) {
            val x = cursor.getString(0)
            res = "$res.$x"
            bRes = cursor.moveToNext()
        }

        return res
    }

    /*
    ** If this is a SEE build, check that encrypted databases work.
    */
    @Throws(Exception::class)
    private suspend fun seeTest1() {
        if (!SQLiteDatabase.hasCodec()) {
            return
        }

        SQLiteDatabase.deleteDatabase(databaseFile)
        val password = "secretkey"


        SQLiteDatabase.openOrCreateDatabase(databaseFile, null, { password }).use { db ->
            //            db.execSQL("PRAGMA key = 'secretkey'") // use SQLiteDatabase.openOrCreateDatabase(..., password) instead

            db.execSQL("CREATE TABLE t1(x)")
            db.execSQL("INSERT INTO t1 VALUES ('one'), ('two'), ('three')")

//            db.execSQL("PRAGMA rekey = ''") // debug... remove encryption

            testResult("see_test_1.1", readStringFromTable1(db), ".one.two.three")
        }

        testResult("see_test_1.2", dbIsEncrypted(), "encrypted")


        SQLiteDatabase.openOrCreateDatabase(databaseFile, null, { password }).use { db ->
            //            db.execSQL("PRAGMA key = 'secretkey'") // use SQLiteDatabase.openOrCreateDatabase(..., password) instead

            testResult("see_test_1.3", readStringFromTable1(db), ".one.two.three")
        }

        var db: SQLiteDatabase? = null
        var res = "unencrypted"
        try {
            db = SQLiteDatabase.openOrCreateDatabase(databaseFile.path, null)
            readStringFromTable1(db)
        } catch (e: SQLiteDatabaseCorruptException) {
            res = "encrypted"
        } finally {
            db?.close()
        }
        testResult("see_test_1.4", res, "encrypted")

        res = "unencrypted"
        try {
            db = SQLiteDatabase.openOrCreateDatabase(databaseFile.path, null, null, { "otherkey" })
//            db.execSQL("PRAGMA key = 'otherkey'") // use SQLiteDatabase.openOrCreateDatabase(..., password) instead
            readStringFromTable1(db)
        } catch (e: SQLiteDatabaseCorruptException) {
            res = "encrypted"
        } finally {
            db?.close()
        }
        testResult("see_test_1.5", res, "encrypted")
    }

    internal inner class MyHelper(ctx: Context) : SQLiteOpenHelper(ctx, databaseFile.path, null, 1) {
        override fun onConfigure(db: SQLiteDatabase) {
//            db.execSQL("PRAGMA key = 'secret'") // override getPassword() instead
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE t1(x)")
        }

        override fun getPassword(): String? {
            return "secret"
        }

        override fun onUpgrade(db: SQLiteDatabase, iOld: Int, iNew: Int) {}
    }

    /*
    ** If this is a SEE build, check that SQLiteOpenHelper still works.
    */
    @Throws(Exception::class)
    private suspend fun seeTest2() {
        if (!SQLiteDatabase.hasCodec()) {
            return
        }

        SQLiteDatabase.deleteDatabase(databaseFile)

        var helper = MyHelper(this)
        var db = helper.writableDatabase
        db.execSQL("INSERT INTO t1 VALUES ('x'), ('y'), ('z')")

        val res = readStringFromTable1(db)
        testResult("see_test_2.1", res, ".x.y.z")
        testResult("see_test_2.2", dbIsEncrypted(), "encrypted")

        helper.close()
        helper = MyHelper(this)
        db = helper.readableDatabase
        testResult("see_test_2.3", res, ".x.y.z")

        db = helper.writableDatabase
        testResult("see_test_2.4", res, ".x.y.z")

        testResult("see_test_2.5", dbIsEncrypted(), "encrypted")
    }

    /**
     * Test html tokenizer, for searching on html elements (should not find anything)
     */
    @Throws(Exception::class)
    private suspend fun ftsTest1() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->

            db.registerTokenizer(Tokenizer.HTML_TOKENIZER)

            db.execSQL("CREATE VIRTUAL TABLE v1 USING fts4(name, tokenize=HTMLTokenizer)")

            val names = resources.getStringArray(R.array.html)

            for (name in names) {
                db.execSQL("INSERT INTO v1 VALUES ('$name')")
            }

            db.rawQuery("SELECT * FROM v1 WHERE name MATCH ?", arrayOf("body")).use { cursor ->

                if (cursor != null && cursor.moveToFirst()) {
                    testResult("fts_text_1.0", cursor.count.toString(), "0")
                } else {
                    testResult("fts_text_1.0", "0", "0")
                }

                cursor?.close()
            }
        }
    }

    /**
     * Test html tokenizer, for good results
     */
    @Throws(Exception::class)
    private suspend fun ftsTest2() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->

            db.registerTokenizer(Tokenizer.HTML_TOKENIZER)

            db.execSQL("CREATE VIRTUAL TABLE v1 USING fts4(name, tokenize=HTMLTokenizer stemmer=english)")

            db.execSQL("INSERT INTO v1 VALUES('<html> Adrenaline <b>Junkies</b> Unite </html>')")
            db.execSQL("INSERT INTO v1 VALUES('<html> Linux Nerds Reunion </html>')")
            db.execSQL("INSERT INTO v1 VALUES('<html> Penicillin Users Assemble </html>')")
            db.execSQL("INSERT INTO v1 VALUES('<html> Burp Man Returns </html>')")
            db.execSQL("INSERT INTO v1 VALUES('<html> Fart Hero Stinks </html>')")
            db.execSQL("INSERT INTO v1 VALUES('<html> Sneeze Scars Massage </html>')")
            db.execSQL("INSERT INTO v1 VALUES('<html> Leian Solo Falls </html>')")
            db.execSQL("INSERT INTO v1 VALUES('<html> Bob Unites Jobs </html>')")

            val expectedResult1 = "2"
            db.rawQuery("SELECT * FROM v1 WHERE name MATCH ?", arrayOf("unite")).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    testResult("fts_text_2.0", cursor.count.toString(), expectedResult1)
                } else {
                    testResult("fts_text_2.0", "0", expectedResult1)
                }
            }

            // test to make sure we can't search on html elements
            val expectedResult2 = "0"
            db.rawQuery("SELECT * FROM v1 WHERE name MATCH ?", arrayOf("html")).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    testResult("fts_text_2.1", cursor.count.toString(), expectedResult2)
                } else {
                    testResult("fts_text_2.1", "0", expectedResult2)
                }
            }
        }
    }

    /**
     * Test rank function
     */
    @Throws(Exception::class)
    private suspend fun ftsTest3() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->

            db.registerTokenizer(Tokenizer.HTML_TOKENIZER)

            db.execSQL("CREATE VIRTUAL TABLE people USING fts4(title, name, tokenize=HTMLTokenizer)")

            db.execSQL("INSERT INTO people VALUES('Boss', '<html> Adrenaline <b>Junkies</b> Unite </html>')")
            db.execSQL("INSERT INTO people VALUES('User', '<html> Linux Nerds Reunion </html>')")
            db.execSQL("INSERT INTO people VALUES('User', '<html> Penicillin Users Assemble </html>')")
            db.execSQL("INSERT INTO people VALUES('User', '<html> Burp Boss Man Returns </html>')")
            db.execSQL("INSERT INTO people VALUES('User', '<html> Fart Hero Stinks </html>')")
            db.execSQL("INSERT INTO people VALUES('User', '<html> Sneeze Scars Massage </html>')")
            db.execSQL("INSERT INTO people VALUES('User', '<html> Leian Solo Falls </html>')")
            db.execSQL("INSERT INTO people VALUES('Boss', '<html> Bob Boss Unites Jobs Boss </html>')")

            db.rawQuery("SELECT * FROM people WHERE people MATCH ? ORDER BY ftsrank(matchinfo(people), 1.0, 0.5) DESC", arrayOf("boss")).use { cursor ->
//                cursor.moveToFirst()
//                do {
//                    val title = cursor.getString(cursor.getColumnIndex("title"))
//                    val name = cursor.getString(cursor.getColumnIndex("name"))
//                    Log.e("TEST", "Title: $title    Name: $name")
//                } while (cursor.moveToNext())

                if (cursor != null && cursor.moveToFirst()) {
                    testResult("fts_text_3.0", cursor.count.toString(), "3")

                    val title = cursor.getString(cursor.getColumnIndex("title"))
                    val name = cursor.getString(cursor.getColumnIndex("name"))

                    testResult("fts_text_3.1", title, "Boss")
                    testResult("fts_text_3.2", name, "<html> Bob Boss Unites Jobs Boss </html>")
                } else {
                    testResult("fts_text_3.0", "0", "1")
                }
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun suppCharTest1() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        var res = ""
        val smiley = String(Character.toChars(0x1F601))

        db.execSQL("CREATE TABLE t1(x)")
        db.execSQL("INSERT INTO t1 VALUES ('a" + smiley + "b')")

        res = stringFromT1X(db)

        testResult("supp_char_test1.$smiley", res, ".a" + smiley + "b")

        db.close()
    }

    @Throws(Exception::class)
    private suspend fun suppCharTest2() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        var res = ""
        val smiley = String(Character.toChars(0x1F638))

        db.execSQL("CREATE TABLE t1(x)")
        db.execSQL("INSERT INTO t1 VALUES ('a" + smiley + "b')")

        res = stringFromT1X(db)

        testResult("supp_char_test2.$smiley", res, ".a" + smiley + "b")

        db.close()
    }

    @Throws(Exception::class)
    private suspend fun stemmerTest1() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->

            db.registerTokenizer(Tokenizer.HTML_TOKENIZER, "eng")

            db.execSQL("CREATE VIRTUAL TABLE stemmer USING fts4(text, tokenize=HTMLTokenizer)")

            db.execSQL("INSERT INTO stemmer VALUES('<html> I NEED TO REPENT </html>')")
            db.execSQL("INSERT INTO stemmer VALUES('<html> I NEED TO BE FAITHFUL AND OBEDIENT </html>')")


            db.rawQuery("SELECT * FROM stemmer WHERE text MATCH ?", arrayOf("REPENTANCE")).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    testResult("stemmerTest1", cursor.count.toString(), "1")

                    val text = cursor.getString(cursor.getColumnIndex("text"))

                    testResult("stemmerTest1", text, "<html> I NEED TO REPENT </html>")
                } else {
                    testResult("stemmerTest1", "0", "1")
                }
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun stemmerTest2() {
        SQLiteDatabase.deleteDatabase(databaseFile)
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->

            db.registerTokenizer(Tokenizer.HTML_TOKENIZER, "spa")

            db.execSQL("CREATE VIRTUAL TABLE stemmer USING fts4(text, tokenize=HTMLTokenizer)")

            db.execSQL("INSERT INTO stemmer VALUES('<html> TIENE QUE ARREPENTIRSE </html>')")
            db.execSQL("INSERT INTO stemmer VALUES('<html> NECESSITO UN LLAMAMIENTO </html>')")


            db.rawQuery("SELECT * FROM stemmer WHERE text MATCH ?", arrayOf("ARREPENTIMIENTO")).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    testResult("stemmerTest2", cursor.count.toString(), "1")

                    val text = cursor.getString(cursor.getColumnIndex("text"))

                    testResult("stemmerTest2", text, "<html> TIENE QUE ARREPENTIRSE </html>")
                } else {
                    testResult("stemmerTest2", "0", "1")
                }
            }
        }
    }

    private suspend fun stringFromT1X(db: SQLiteDatabase): String {
        var res = ""

        val c = db.rawQuery("SELECT x FROM t1", null)
        var bRes: Boolean
        bRes = c.moveToFirst()
        while (bRes) {
            val x = c.getString(0)
            res = "$res.$x"
            bRes = c.moveToNext()
        }

        return res
    }

    private suspend fun appendString(data: String) = withContext(Dispatchers.Main) {
        logTextView.append(data)
    }
}


