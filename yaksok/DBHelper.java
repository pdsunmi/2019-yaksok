package com.example.yaksok;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public DBHelper(Context context){
        super(context, "yiddb", null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqldb) {
        String idSQL = "create table IdTabel" +
                "(_num integer primary key autoincrement, " +
                "_docid text UNIQUE)" ;
        sqldb.execSQL(idSQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqldb, int oldVersion, int newVersion) {
        if(newVersion == DATABASE_VERSION){
            sqldb.execSQL("drop table tb_id");
            onCreate(sqldb);
        }
    }
}
