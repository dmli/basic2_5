package com.ldm.basic.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ldm on 12-9-17. 数据库助手，提供了基础的的增删改查功能，
 * 开发中可以将配置信息写在raw文件夹下的db.properties中
 */
public class BasicSQLiteOpenHelper extends SQLiteOpenHelper {


    private static final Map<String, BasicSQLiteOpenHelper> dbs = new HashMap<>();
    private IDBAccess access;

    BasicSQLiteOpenHelper(IDBAccess access) {
        super(access.getContext(), access.getDbName(), null, access.getDbVersion());
        this.access = access;
    }

    public static BasicSQLiteOpenHelper getInstance(IDBAccess access) {
        BasicSQLiteOpenHelper helper = null;
        if (dbs.containsKey(access.getDbName())) {
            helper = dbs.get(access.getDbName());
        }
        if (helper == null) {
            helper = new BasicSQLiteOpenHelper(access);
            dbs.put(access.getDbName(), helper);
        }
        return helper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (access != null) {
            access.create(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (access != null) {
            access.update(db, oldVersion, newVersion);
        }
    }

    /**
     * insert
     *
     * @param sql sql
     * @param obj p
     */
    public synchronized void insert(String sql, Object[] obj) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                db.execSQL(sql, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * update
     *
     * @param sql sql
     * @param obj p
     */
    public synchronized void update(String sql, Object[] obj) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                db.execSQL(sql, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * delete
     *
     * @param sql sql
     * @param obj p
     */
    public synchronized void delete(String sql, Object[] obj) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                db.execSQL(sql, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * delete
     *
     * @param table       表名
     * @param whereClause 条件
     * @param whereArgs   参数
     * @return 成功数
     */
    public synchronized int delete(String table, String whereClause, String[] whereArgs) {
        int result = 0;
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                result = db.delete(table, whereClause, whereArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * query
     *
     * @param table         表名
     * @param columns       字段s
     * @param selection     条件
     * @param selectionArgs 参数
     * @param groupBy       分组
     * @param having        聚合
     * @param orderBy       排序
     * @return 游标
     */
    public synchronized Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            if (db != null) {
                return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * query
     *
     * @param table         表名
     * @param columns       字段
     * @param selection     条件
     * @param selectionArgs 参数
     * @param groupBy       分组
     * @param having        聚合
     * @param orderBy       排序
     * @param limit         分页
     * @return 游标
     */
    public synchronized Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            if (db != null) {
                return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * rawQuery
     *
     * @param sql           sql
     * @param selectionArgs 参数
     * @return 游标
     */
    public synchronized Cursor rawQuery(String sql, String[] selectionArgs) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            if (db != null) {
                return db.rawQuery(sql, selectionArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 尝试关闭默认配置下的数据库
     */
    public synchronized static void closeDB() {
        /**
         * 尝试释放dbs中的数据库对象
         */
        synchronized (dbs) {
            Set<String> set = dbs.keySet();
            for (String s : set) {
                try {
                    // 检查数据库中是都有等待提交的事务
                    BasicSQLiteOpenHelper soh = dbs.get(s);
                    SQLiteDatabase db = soh.getWritableDatabase();
                    if (db != null && db.isDbLockedByCurrentThread()) {
                        db.endTransaction();
                    }
                    soh.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
