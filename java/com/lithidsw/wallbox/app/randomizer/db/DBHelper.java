/*
 * Copyright 2014 Jeremie Long
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lithidsw.wallbox.app.randomizer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "randomize.db";
    private static final int DATABASE_VERSION = 1;

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    public static final String TABLE_CURRENT_PAPERS = "papers";

    public static final String C_PATH = "path";
    public static final String C_MD5 = "md5";

    private static final String DATABASE_CREATE_PAPERS = "create table "
            + TABLE_CURRENT_PAPERS + "(" + C_PATH + " TEXT NOT NULL, " + C_MD5 + " TEXT NOT NULL);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_PAPERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE + TABLE_CURRENT_PAPERS);
        onCreate(db);
    }
}
