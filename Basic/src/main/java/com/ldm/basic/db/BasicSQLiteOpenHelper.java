package com.ldm.basic.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ldm.basic.app.Configuration;
import com.ldm.basic.properties.PropertiesHelper;
import com.ldm.basic.utils.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by ldm on 12-9-17. 数据库助手，提供了基础的的增删改查功能，
 * 开发中可以将配置信息写在raw文件夹下的db.properties中
 */
public class BasicSQLiteOpenHelper extends SQLiteOpenHelper {


    private static Map<String, BasicSQLiteOpenHelper> dbs = new HashMap<>();
    private static BasicSQLiteOpenHelper dbHelper;

    private String dbCallbackPackage;
    private DBCallback dbCallback;

    private BasicSQLiteOpenHelper(Context context, String dbName, int dbVersion, String dbCallbackPackage) {
        super(context, dbName, null, dbVersion);
        this.dbCallbackPackage = dbCallbackPackage;
    }

    /**
     * 返回DB的实例 ***使用DBConfig的配置信息且单例模式***
     *
     * @param context Context
     * @param config  数据库配置
     * @return BasicSQLiteOpenHelper
     */
    public static BasicSQLiteOpenHelper getInstance(Context context, DBConfig config) {
        BasicSQLiteOpenHelper db = dbs.get(config.getDbName());
        if (!dbs.containsKey(config.getDbName()) || db == null) {
            db = new BasicSQLiteOpenHelper(context, config.getDbName(), config.getDbVersion(), config.getDbCallbackPackage());
            dbs.put(config.getDbName(), db);
        }
        return db;
    }

    /**
     * 返回DB的实例 ***使用默认的配置信息且单例模式***
     *
     * @param context Context
     * @return BasicSQLiteOpenHelper
     */
    public static BasicSQLiteOpenHelper getInstance(Context context) {
        if (dbHelper == null) {
            Properties proper = PropertiesHelper.loadProperties(context, Configuration.DB_CONFIG_FILE_NAME, "raw", context.getPackageName());
            if (proper != null) {
                String dbName = PropertiesHelper.get(proper, Configuration.DB_NAME_KEY);
                int dbVersion = 0;
                try {
                    dbVersion = Integer.parseInt(PropertiesHelper.get(proper, Configuration.DB_VERSION_KEY));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                dbHelper = new BasicSQLiteOpenHelper(context, dbName, dbVersion, PropertiesHelper.get(proper, Configuration.DB_CALLBACK_KEY));
            } else {
                Log.e("数据库初始化失败，请检查配置文件是否正确!");
                dbHelper = null;
            }
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (dbCallback == null) {
            if (initDBCallback(dbCallbackPackage)) {
                dbCallback.create(db);
                Log.e("数据库创建成功！");
            } else {
                Log.e("数据库创建失败！");
            }
        } else {
            dbCallback.create(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (dbCallback == null) {
            if (initDBCallback(dbCallbackPackage)) {
                dbCallback.update(db, oldVersion, newVersion);
                Log.e("数据库修改成功！");
            } else {
                Log.e("数据库修改失败！");
            }
        } else {
            dbCallback.update(db, oldVersion, newVersion);
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
                synchronized (db) {
                    db.execSQL(sql, obj);
                }
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
                synchronized (db) {
                    db.execSQL(sql, obj);
                }
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
                synchronized (db) {
                    db.execSQL(sql, obj);
                }
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
                synchronized (db) {
                    result = db.delete(table, whereClause, whereArgs);
                }
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
                synchronized (db) {
                    return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
                }
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
                synchronized (db) {
                    return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
                }
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
                synchronized (db) {
                    return db.rawQuery(sql, selectionArgs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化数据库回调接口
     *
     * @param classes 类的全路径
     * @return true初始化成功
     */
    private boolean initDBCallback(String classes) {
        boolean result = false;
        try {
            Class<?> s = Class.forName(classes);
            dbCallback = (DBCallback) s.newInstance();
            result = true;
        } catch (ClassNotFoundException e) {
            Log.e("没有找到 [" + classes + "] 请检查配置文件！");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(" [" + classes + "] newInstance() 时出现错误！");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 尝试关闭默认配置下的数据库
     */
    public synchronized static void closeDB() {
        if (dbHelper != null) {
            try {
                // 检查数据库中是都有等待提交的事务
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (db != null && db.isDbLockedByCurrentThread()) {
                    db.endTransaction();
                }
                dbHelper.close();
                dbHelper = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            /**
             * 尝试释放dbs中的数据库对象
             */
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

    /**
     * 数据库创建时的接口，用户需要继承该接口实现对数据库的创建及修改
     */
    public interface DBCallback {
        void create(SQLiteDatabase db);

        void update(SQLiteDatabase db, int oldVersion, int newVersion);
    }
}
