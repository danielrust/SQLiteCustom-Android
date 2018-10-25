package org.sqlite.database.sqlite;

import androidx.annotation.Nullable;

public interface SQLiteEncryptionExtension {
    @Nullable
    String getPassword();
}
