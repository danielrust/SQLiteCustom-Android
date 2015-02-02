/*
** 2012 November 11
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
**
******************************************************************************
**
*/
#include "../sqlite3.h"
#include "../sqlite3ext.h"
#include <android/log.h>

#include "unicodesn/fts3_unicodesn.h"
#include "character/character_tokenizer.h"
#include "html/fts3_html_tokenizer_main.h"

SQLITE_EXTENSION_INIT1

/*
** Register a tokenizer implementation with FTS3 or FTS4.
*/
static int registerExtensionTokenizer(
  sqlite3 *db,
  char *zName,
  const sqlite3_tokenizer_module *p
){
  int rc;
  sqlite3_stmt *pStmt;
  const char *zSql = "SELECT fts3_tokenizer(?, ?)";

  rc = sqlite3_prepare_v2(db, zSql, -1, &pStmt, 0);
  if( rc!=SQLITE_OK ){
    return rc;
  }

  sqlite3_bind_text(pStmt, 1, zName, -1, SQLITE_STATIC);
  sqlite3_bind_blob(pStmt, 2, &p, sizeof(p), SQLITE_STATIC);
  sqlite3_step(pStmt);

  return sqlite3_finalize(pStmt);
}

/* SQLite invokes this routine once when it loads the extension.
** Create new functions, collating sequences, and virtual table
** modules here.  This is usually the only exported symbol in
** the shared library.
*/
int sqlite3_extension_init(
      sqlite3 *db,          /* The database connection */
      char **pzErrMsg,      /* Write error messages here */
      const sqlite3_api_routines *pApi  /* API methods */
      )
{
   const sqlite3_tokenizer_module *tokenizer;

   SQLITE_EXTENSION_INIT2(pApi)

   sqlite3Fts3UnicodeTokenizer(&tokenizer);

   registerExtensionTokenizer(db, HTML_NAME, tokenizer);

   return 0;
}

int sqlite3_extension_init_character(
      sqlite3 *db,          /* The database connection */
      char **pzErrMsg,      /* Write error messages here */
      const sqlite3_api_routines *pApi  /* API methods */
      )
{
   const sqlite3_tokenizer_module *tokenizer;

   SQLITE_EXTENSION_INIT2(pApi)

   get_character_tokenizer_module(&tokenizer);

   registerExtensionTokenizer(db, CHARACTER_NAME, tokenizer);

   return 0;
}

int sqlite3_extension_init_unicodesn(
      sqlite3 *db,          /* The database connection */
      char **pzErrMsg,      /* Write error messages here */
      const sqlite3_api_routines *pApi  /* API methods */
      )
{
   const sqlite3_tokenizer_module *tokenizer;

   SQLITE_EXTENSION_INIT2(pApi)

   sqlite3Fts3UnicodeSnTokenizer(&tokenizer);

   registerExtensionTokenizer(db, TOKENIZER_NAME, tokenizer);

   return 0;
}


