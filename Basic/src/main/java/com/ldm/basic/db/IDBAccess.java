package com.ldm.basic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ldm on 12-9-17.
 * 创建BasicSQLiteOpenHelper时使用的配置
 */
public interface IDBAccess {

    /**
     * 数据库名称
     *
     * @return str
     */
    String getDbName();

    /**
     * 数据库版本
     *
     * @return int
     */
    int getDbVersion();

    Context getContext();

    void create(SQLiteDatabase db);

    void update(SQLiteDatabase db, int oldVersion, int newVersion);

}
