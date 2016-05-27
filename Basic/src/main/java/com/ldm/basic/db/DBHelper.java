package com.ldm.basic.db;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.ldm.basic.utils.LLog;
import com.ldm.basic.utils.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ldm on 14-2-7.
 * 数据库助手，提供了简单的数据表与实体绑定、自动生成建表语句及sql语句批处理功能
 */
public class DBHelper {

    /**
     * 删除表
     *
     * @param db   SQLiteDatabase
     * @param name 表名
     */
    public static void dropTable(SQLiteDatabase db, String name) {
        try {
            db.execSQL("DROP TABLE " + name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回指定表是否存在
     *
     * @param db        SQLiteDatabase
     * @param tableName 表明
     * @return true表存在成功
     */
    public static boolean isTableExist(SQLiteDatabase db, String tableName) {
        if (db == null || tableName == null) {
            return false;
        }
        boolean result = false;
        Cursor cursor;
        try {
            String sql = "select count(*) as c from sqlite_master where type = ? and name = ?";
            cursor = db.rawQuery(sql, new String[]{"table", tableName.trim()});
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
            cursor.close();
        } catch (Exception e) {
            LLog.e("DBHelper.isTableExist", "not find " + tableName);
        }
        return result;
    }

    /**
     * 根据给定的Class的所有属性 返回一个用于创建数据表的sql语句 仅区分String与int类型属性
     *
     * @param c Class
     * @return sql
     */
    public static String genCreateTableSql(final Class<? extends BasicTable> c) {
        return genCreateTableSql(c, null);
    }

    /**
     * 根据给定的Class的所有属性 返回一个用于创建数据表的sql语句 仅区分String与int类型属性
     *
     * @param c         Class
     * @param tableName 表名，可以为null
     * @return sql
     */
    public static String genCreateTableSql(final Class<? extends BasicTable> c, final String tableName) {
        if (c == null)
            return null;
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(getTableName(tableName, c));
        sql.append(" ( ");

        List<ColumnRes> columnRes = getColumnsField(c, true);// 获取Class<?>及父类的所有满足条件的属性
        int primaryKey = 0;
        String key = "";
        for (ColumnRes columnRe : columnRes) {
            if (columnRe.isPrimaryKey()) {
                primaryKey++;
                key += "," + columnRe.getName();
            }
        }
        boolean isFirst = true;
        for (ColumnRes columnRe : columnRes) {
            if (isFirst) {// 第一次不加逗号
                sql.append(" ");
            } else {
                sql.append(", ");
            }
            sql.append(columnRe.getName());
            sql.append(" ");
            sql.append(columnRe.getType());
            if (!Column._DEFAULT_VALUE.equals(columnRe.getDefaultValue())) {
                sql.append(" default ");
                sql.append(String.valueOf("'" + columnRe.getDefaultValue() + "'"));
            }
            if (columnRe.isNotNull()) {
                sql.append(" NOT NULL");
            }
            if (primaryKey <= 1) {
                if (columnRe.isPrimaryKey()) {
                    sql.append(" PRIMARY KEY");
                    if (columnRe.isAutoIncrement()) {
                        sql.append(" AUTOINCREMENT");
                    }
                }
            }
            isFirst = false;
        }
        if (primaryKey > 1) {
            sql.append(", PRIMARY KEY(");
            sql.append(key.substring(1, key.length()));
            sql.append(")");
        }
        sql.append(") ");
        return sql.toString();
    }

    /**
     * 将Cursor与给定的Class<T>进行绑定，返回List<T> 仅区分String与int类型属性 *大数据量时不建议使用*
     *
     * @param c   Cursor
     * @param ct  Class<T> 与Cursor进行绑定的载体
     * @param <T> 载体的类型，必需继承BasicTable的数据Bean
     * @return List<T>
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static <T extends BasicTable> List<T> fromCursor(Cursor c, final Class<T> ct) throws IllegalAccessException, InstantiationException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        if (c == null) {
            return null;
        }
        if (c.getCount() == 0 || !c.moveToFirst()) {
            c.close();
            return null;
        }
        List<T> result = new ArrayList<>();
        do {
            int len = c.getColumnCount();
            Map<String, Class<?>> columnRes = getColumnsField2(ct, true);// 获取Class<?>及父类的所有满足条件的属性
            T t = ct.newInstance();
            for (int i = 0; i < len; i++) {
                String name = c.getColumnName(i);
                String methodName = "set" + TextUtils.upperFirst(name);
                if (columnRes.containsKey(name)) {
                    Method m = ct.getMethod(methodName, columnRes.get(name));
                    String simpleName = columnRes.get(name).getSimpleName();
                    if ("int".equals(simpleName) || "Integer".equals(simpleName)) {
                        m.invoke(t, c.getInt(c.getColumnIndex(name)));
                    } else if ("long".equals(simpleName) || "Long".equals(simpleName)) {
                        m.invoke(t, c.getLong(c.getColumnIndex(name)));
                    } else if ("float".equals(simpleName) || "Float".equals(simpleName)) {
                        m.invoke(t, c.getFloat(c.getColumnIndex(name)));
                    } else if ("double".equals(simpleName) || "Double".equals(simpleName)) {
                        m.invoke(t, c.getDouble(c.getColumnIndex(name)));
                    } else {
                        m.invoke(t, c.getString(c.getColumnIndex(name)));
                    }
                }
            }
            result.add(t);
        } while (c.moveToNext());
        c.close();
        return result;
    }

    /**
     * 查询指定表中的指定字段，通过getString获取
     *
     * @param db         BasicSQLiteOpenHelper
     * @param tableName  表名
     * @param columnName 字段名
     * @return List<String>
     */
    public static List<String> queryOnlyColumn(BasicSQLiteOpenHelper db, String tableName, String columnName) {
        return queryOnlyColumn(db, tableName, columnName, null, null);
    }

    /**
     * 查询指定表中的指定字段，通过getString获取
     *
     * @param db         BasicSQLiteOpenHelper
     * @param tableName  表名
     * @param columnName 字段名
     * @param where      条件
     * @param param      参数
     * @return List<String>
     */
    public static List<String> queryOnlyColumn(BasicSQLiteOpenHelper db, String tableName, String columnName, String where, String[] param) {
        Cursor c = db.rawQuery("select " + columnName + " from " + tableName + (where == null ? "" : "where " + where), param);
        if (c != null) {
            if (c.getCount() > 0 && c.moveToFirst()) {
                List<String> cs = new ArrayList<>();
                int columnIndex = c.getColumnIndex(columnName);
                do {
                    cs.add(c.getString(columnIndex));
                } while (c.moveToNext());
                return cs;
            }
            c.close();
        }

        return null;
    }

    /**
     * 在游标中取出某个字段，通过getString获取
     *
     * @param c          Cursor
     * @param columnName 字段名
     * @return List<String>
     */
    public static List<String> queryOnlyColumn(Cursor c, String columnName) {
        if (c != null) {
            if (c.getCount() > 0 && c.moveToFirst()) {
                List<String> cs = new ArrayList<>();
                int columnIndex = c.getColumnIndex(columnName);
                do {
                    cs.add(c.getString(columnIndex));
                } while (c.moveToNext());
                c.close();
                return cs;
            }
            c.close();
        }
        return null;
    }


    public static <T extends BasicTable> T fromCursorOnly(Cursor c, final Class<T> ct) throws IllegalAccessException, InstantiationException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        if (c == null) {
            return null;
        }
        if (c.getCount() == 0 || !c.moveToFirst()) {
            c.close();
            return null;
        }
        T t;
        int len = c.getColumnCount();
        Map<String, Class<?>> columnRes = getColumnsField2(ct, true);// 获取Class<?>及父类的所有满足条件的属性

        t = ct.newInstance();
        for (int i = 0; i < len; i++) {
            String name = c.getColumnName(i);
            String methodName = "set" + TextUtils.upperFirst(name);
            if (columnRes.containsKey(name)) {
                Method m = ct.getMethod(methodName, columnRes.get(name));
                String simpleName = columnRes.get(name).getSimpleName();
                if ("int".equals(simpleName) || "Integer".equals(simpleName)) {
                    m.invoke(t, c.getInt(c.getColumnIndex(name)));
                } else if ("long".equals(simpleName) || "Long".equals(simpleName)) {
                    m.invoke(t, c.getLong(c.getColumnIndex(name)));
                } else if ("float".equals(simpleName) || "Float".equals(simpleName)) {
                    m.invoke(t, c.getFloat(c.getColumnIndex(name)));
                } else if ("double".equals(simpleName) || "Double".equals(simpleName)) {
                    m.invoke(t, c.getDouble(c.getColumnIndex(name)));
                } else {
                    m.invoke(t, c.getString(c.getColumnIndex(name)));
                }
            }
        }
        c.close();
        return t;
    }

    /**
     * 根据Class<T>查询表数据，该方法使用默认数据库且使用*号查询
     *
     * @param db  BasicSQLiteOpenHelper
     * @param ct  表映射的类
     * @param <T> T extends BasicTable
     * @return List<T>
     * @throws Exception
     */
    public static <T extends BasicTable> List<T> queryToClass(final BasicSQLiteOpenHelper db, final Class<T> ct) throws Exception {
        return queryToClass(db, ct.getSimpleName(), ct, null);
    }

    /**
     * 根据Class<T>查询表数据，该方法使用默认数据库且使用*号查询
     *
     * @param db        BasicSQLiteOpenHelper
     * @param tableName 表名，可以为null
     * @param ct        表映射的类
     * @param <T>       T extends BasicTable
     * @return List<T>
     * @throws Exception
     */
    public static <T extends BasicTable> List<T> queryToClass(final BasicSQLiteOpenHelper db, final String tableName, final Class<T> ct) throws Exception {
        return queryToClass(db, tableName, ct, null);
    }

    /**
     * 根据Class<T>查询表数据，该方法使用默认数据库且使用*号查询
     *
     * @param db  BasicSQLiteOpenHelper
     * @param ct  表映射的类
     * @param <T> T extends BasicTable
     * @return List<T>
     * @throws Exception
     */
    public static <T extends BasicTable> List<T> queryToClass(final BasicSQLiteOpenHelper db, final Class<T> ct, final String order) throws Exception {
        return queryToClass(db, ct.getSimpleName(), ct, order);
    }

    /**
     * 根据Class<T>查询表数据，该方法使用默认数据库且使用*号查询
     *
     * @param db        BasicSQLiteOpenHelper
     * @param tableName 表名，可以为null
     * @param ct        表映射的类
     * @param <T>       T extends BasicTable
     * @return List<T>
     * @throws Exception
     */
    public static <T extends BasicTable> List<T> queryToClass(final BasicSQLiteOpenHelper db, final String tableName, final Class<T> ct, final String order) throws Exception {
        return fromCursor(db.rawQuery("SELECT * FROM " + getTableName(tableName, ct) + (order == null ? "" : " order by " + order), null), ct);
    }

    private static <T extends BasicTable> String getTableName(final String tableName, final Class<T> ct) {
        return (TextUtils.isNull(tableName) ? ct.getSimpleName() : tableName).toUpperCase(Locale.CHINA);
    }

    /**
     * 根据Class<T>查询表数据，该方法使用默认数据库且使用*号查询
     *
     * @param db    BasicSQLiteOpenHelper
     * @param ct    表映射的类
     * @param where 条件
     * @param param 条件对应的参数
     * @param <T>   T extends BasicTable
     * @return List<T>
     * @throws Exception
     */
    public static <T extends BasicTable> List<T> queryToClass(final BasicSQLiteOpenHelper db, final Class<T> ct, final String where, final String[] param) throws Exception {
        return queryToClass(db, ct.getSimpleName(), ct, where, param, null);
    }

    /**
     * 根据Class<T>查询表数据，该方法使用默认数据库且使用*号查询
     *
     * @param db        BasicSQLiteOpenHelper
     * @param tableName 表名，可以为null
     * @param ct        表映射的类
     * @param where     条件
     * @param param     条件对应的参数
     * @param order     排序
     * @param <T>       T extends BasicTable
     * @return List<T>
     * @throws Exception
     */
    public static <T extends BasicTable> List<T> queryToClass(final BasicSQLiteOpenHelper db, final String tableName, final Class<T> ct, final String where, final String[] param, final String order) throws Exception {
        return fromCursor(db.rawQuery("SELECT * FROM " + getTableName(tableName, ct) + (where == null ? "" : " WHERE " + where) + (order == null ? "" : " order by " + order), param), ct);
    }

    /**
     * 根据Class<T>查询表数据，该方法使用默认数据库且使用*号查询
     *
     * @param db        BasicSQLiteOpenHelper
     * @param tableName 表名，可以为null
     * @param ct        表映射的类
     * @param where     条件
     * @param param     条件对应的参数
     * @param order     排序
     * @param pager     分页[0]offset数 [1]每页条数
     * @param <T>       T extends BasicTable
     * @return List<T>
     * @throws Exception
     */
    public static <T extends BasicTable> List<T> queryToClass(final BasicSQLiteOpenHelper db, final String tableName, final Class<T> ct, final String where, final String[] param, final String order, int[] pager) throws Exception {
        List<String> params = new ArrayList<>();
        if (param != null) {
            Collections.addAll(params, param);
        }
        for (int i : pager) {
            params.add(i + "");
        }
        return fromCursor(db.rawQuery("SELECT * FROM " + getTableName(tableName, ct) + (where == null ? "" : " WHERE " + where) + (order == null ? "" : " order by " + order) + " limit ?, ?", params.toArray(new String[params.size()])), ct);
    }

    /**
     * @param db    BasicSQLiteOpenHelper
     * @param ct    表映射的类
     * @param where 条件
     * @param param 条件对应的参数
     * @param <T>   T extends BasicTable
     * @return T
     * @throws Exception
     */
    public static <T extends BasicTable> T queryOnlyToClass(final BasicSQLiteOpenHelper db, final Class<T> ct, final String where, final String[] param) throws Exception {
        return queryOnlyToClass(db, ct.getSimpleName(), ct, where, param);
    }

    /**
     * @param db        BasicSQLiteOpenHelper
     * @param tableName 表名，可以为null
     * @param ct        表映射的类
     * @param where     条件
     * @param param     条件对应的参数
     * @param <T>       T extends BasicTable
     * @return T
     * @throws Exception
     */
    public static <T extends BasicTable> T queryOnlyToClass(final BasicSQLiteOpenHelper db, final String tableName, final Class<T> ct, final String where, final String[] param) throws Exception {
        return fromCursorOnly(db.rawQuery("SELECT * FROM " + getTableName(tableName, ct) + (where == null ? "" : " WHERE " + where), param), ct);
    }

    /**
     * 递归查询给定Class及父类的所有带有@Column注解的属性
     *
     * @param c         Class<?>
     * @param recursion true进行递归查询
     * @return List<ColumnRes>
     */
    public static List<ColumnRes> getColumnsField(final Class<?> c, final boolean recursion) {
        List<ColumnRes> field = new ArrayList<>();
        if (c.getSuperclass() != null) {
            Column col = c.getAnnotation(Column.class);
            for (Field f : c.getDeclaredFields()) {
                if (f.getAnnotation(Column.class) == null)
                    continue;// 忽略没有注解的属性
                if ("_id".equals(f.getName()) && col != null && col.ignoreAutoPrimaryKey()) {
                    continue;// 忽略自增长字段
                }
                Column column = f.getAnnotation(Column.class);
                field.add(new ColumnRes(f.getName(), column.type(), column.notNull(), column.primaryKey(), column.autoIncrement(), column.defaultValue()));
            }
            if (recursion) {
                field.addAll(getColumnsField(c.getSuperclass(), true));
            }
        }
        return field;
    }

    /**
     * 将T在数据库中对应的记录做更新操作,使用_id作为条件
     * *这个方法执行时相对较耗时，如果批量入库时请手动编写update的sql语句使用batchExecSQL执行*
     *
     * @param db  BasicSQLiteOpenHelper
     * @param t   记录，必需在数据库中存在
     * @param <T> <T extends BasicTable>
     * @return true 修改成功
     */
    public static <T extends BasicTable> boolean updateToClass(final BasicSQLiteOpenHelper db, T t) {
        return updateToClass(db, null, t);
    }

    /**
     * 将T在数据库中对应的记录做更新操作,使用_id作为条件
     * *这个方法执行时相对较耗时，如果批量入库时请手动编写update的sql语句使用batchExecSQL执行*
     *
     * @param db        BasicSQLiteOpenHelper
     * @param tableName 表名，可以为null
     * @param t         记录，必需在数据库中存在
     * @param <T>       <T extends BasicTable>
     * @return true 修改成功
     */
    public static <T extends BasicTable> boolean updateToClass(final BasicSQLiteOpenHelper db, final String tableName, T t) {
        Map<String, Class<?>> columnRes = getColumnsField2(((Object) t).getClass(), true);// 获取Class<?>及父类的所有满足条件的属性
        Class<?> ct = ((Object) t).getClass();
        String sql = "UPDATE " + (TextUtils.isNull(tableName) ? ct.getSimpleName() : tableName).toUpperCase(Locale.CHINA) + " SET ";
        String w = "";
        List<String> param = new ArrayList<>();
        for (String s : columnRes.keySet()) {
            if (s.equals("_id"))
                continue;
            String methodName = "get" + TextUtils.upperFirst(s);
            try {
                Method m = ct.getMethod(methodName);
                param.add(String.valueOf(m.invoke(t)));
                w += " " + s + " = ?,";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (w.length() <= 1) {
            return false;// 返回更新失败提示
        }
        if (w.length() > 1) {// 去掉最后的,号
            w = w.substring(0, w.length() - 1);
        }
        sql += w + " WHERE _id = ?";
        param.add(t.get_id() + "");
        db.update(sql, param.toArray());
        return true;
    }

    /**
     * 递归查询给定Class及父类的所有带有@Column注解的属性 返回MapMap<String, Class<?>> key = name |
     * value = type
     *
     * @param c         Class<?>
     * @param recursion true进行递归查询
     * @return Map
     */
    public static Map<String, Class<?>> getColumnsField2(final Class<?> c, final boolean recursion) {
        Map<String, Class<?>> field = new HashMap<>();
        if (c.getSuperclass() != null) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getAnnotation(Column.class) == null)
                    continue;
                field.put(f.getName(), f.getType());
            }
            if (recursion) {
                field.putAll(getColumnsField2(c.getSuperclass(), true));
            }
        }
        return field;
    }

    /**
     * 查询指定数据表中的数据条数
     *
     * @param db        BasicSQLiteOpenHelper
     * @param tableName 表名
     * @return count
     */
    public static int queryCount(BasicSQLiteOpenHelper db, String tableName) {
        return queryCount(db, tableName, null, null);
    }

    /**
     * 查询指定数据表中的数据条数
     *
     * @param db BasicSQLiteOpenHelper
     * @param ct 表实体
     * @return count
     */
    public static <T extends BasicTable> int queryCount(BasicSQLiteOpenHelper db, final Class<T> ct) {
        return queryCount(db, BasicTable.getTableName(ct), null, null);
    }

    /**
     * 查询指定数据表中的数据条数
     *
     * @param db    BasicSQLiteOpenHelper
     * @param ct    对应数据表的实体
     * @param where 条件
     * @param param 参数
     * @return count
     */
    public static <T extends BasicTable> int queryCount(BasicSQLiteOpenHelper db, final Class<T> ct, final String where, final String[] param) {
        return queryCount(db, BasicTable.getTableName(ct), where, param);
    }

    /**
     * 查询指定数据表中的数据条数
     *
     * @param db        BasicSQLiteOpenHelper
     * @param tableName 表名
     * @param where     条件
     * @param param     参数
     * @return count
     */
    public static int queryCount(BasicSQLiteOpenHelper db, final String tableName, final String where, final String[] param) {
        int result = 0;
        Cursor c0;
        if (where == null || "".equals(where)) {
            c0 = db.rawQuery("select count(_id) as _id from " + tableName, null);
        } else {
            c0 = db.rawQuery("select count(_id) as _id from " + tableName + " where " + where, param);
        }
        if (c0 != null) {
            if (c0.getCount() > 0 && c0.moveToFirst()) {
                result = c0.getInt(c0.getColumnIndex("_id"));
            }
            c0.close();
        }
        return result;
    }

    /**
     * 根据给定的obj返回一条insert语句（public、FINAL、STATIC访问权限的属性将不被列入其中）
     *
     * @param obj 将要组成sql语句的数据载体,该参数不能为List/map/set等集合
     * @return Map key为sql语句 value为对应sql语句的参数集合
     */
    public static Map<String, String[]> getInsertSql(final Object obj) {
        Map<String, Class<?>> fs = getColumnsField2(obj.getClass(), true);// 不获取Class<?>及父类的所有满足条件的属性
        return getInsertSql(fs, obj);
    }

    /**
     * 根据给定的obj返回一条insert语句（public、FINAL、STATIC访问权限的属性将不被列入其中）
     *
     * @param obj 将要组成sql语句的数据载体,该参数不能为List/map/set等集合
     * @return Map key为sql语句 value为对应sql语句的参数集合
     */
    public static Map<String, String[]> getInsertSql(final Object obj, final String tableName) {
        Map<String, Class<?>> fs = getColumnsField2(obj.getClass(), true);// 不获取Class<?>及父类的所有满足条件的属性
        return getInsertSql(tableName, fs, obj);
    }

    /**
     * 根据给定的规则与类型将obj对象的属性组成一条sql语句
     *
     * @param fs  对象所有属性， 可用过getColumnsField2(obj.getClass(), rules, true)方法获得
     * @param obj 将要组成sql语句的数据载体,该参数不能为List/map/set等集合
     * @return Map key为sql语句 value为对应sql语句的参数集合
     */
    public static Map<String, String[]> getInsertSql(final Map<String, Class<?>> fs, final Object obj) {
        return getInsertSql(BasicTable.getTableName(obj.getClass()), fs, obj);
    }

    /**
     * 根据给定的规则与类型将obj对象的属性组成一条sql语句
     *
     * @param fs        对象所有属性， 可用过getColumnsField2(obj.getClass(), rules, true)方法获得
     * @param tableName 表明
     * @param obj       将要组成sql语句的数据载体,该参数不能为List/map/set等集合
     * @return Map key为sql语句 value为对应sql语句的参数集合
     */
    public static Map<String, String[]> getInsertSql(final String tableName, final Map<String, Class<?>> fs, final Object obj) {
        if (fs == null || obj == null)
            return null;
        Map<String, String[]> result = null;
        if (fs.containsKey("_id")) {
            fs.remove("_id");
        }
        StringBuilder s1 = new StringBuilder();
        s1.append("INSERT INTO ");
        s1.append(tableName);
        s1.append(" ( ");
        // ------------------------------------------------------
        StringBuilder s2 = new StringBuilder();
        s2.append(" VALUES ( ");
        List<String> param = new ArrayList<>();
        for (String name : fs.keySet()) {
            try {
                Method m = obj.getClass().getMethod("get" + TextUtils.upperFirst(name));
                if (m != null) {
                    Object value = m.invoke(obj);
                    if (value != null) {
                        param.add(String.valueOf(value));
                        s1.append(name);
                        s1.append(", ");
                        s2.append("?, ");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String sql1 = s1.toString();
        String sql2 = s2.toString();
        if (sql1.endsWith(", ")) {
            sql1 = sql1.substring(0, sql1.length() - 2);
        }
        if (sql2.endsWith(", ")) {
            sql2 = sql2.substring(0, sql2.length() - 2);
        }
        sql1 += ") ";
        sql2 += ") ";
        if (param.size() > 0) {
            result = new HashMap<>();
            String[] pa = param.toArray(new String[param.size()]);
            result.put(sql1 + sql2, pa);
        }
        return result;
    }

    /**
     * 将给定集合中的对象存储到db中，该对象集合需要是相同的对象否则将无法存储
     *
     * @param db  SQLiteDatabase
     * @param obj Object...
     * @return true成功 false失败
     */
    @SafeVarargs
    public static <T extends BasicTable> boolean saveSameClassToDB(final SQLiteDatabase db, final T... obj) {
        return saveSameClassToDB(db, "", obj);
    }

    /**
     * 将给定集合中的对象存储到db中，该对象集合需要是相同的对象否则将无法存储
     *
     * @param db  SQLiteDatabase
     * @param obj Object...
     * @return true成功 false失败
     */
    @SafeVarargs
    public static synchronized <T extends BasicTable> boolean saveSameClassToDB(final SQLiteDatabase db, final String tableName, final T... obj) {
        boolean result = false;
        if (obj != null && obj.length > 0) {
            Map<String, Class<?>> fs = getColumnsField2(((Object) obj[0]).getClass(), true);// 不获取Class<?>及父类的所有满足条件的属性
            List<String> sql = new ArrayList<>();
            List<String[]> param = new ArrayList<>();
            for (Object o : obj) {
                Map<String, String[]> r;
                if (TextUtils.isNull(tableName)) {
                    r = getInsertSql(fs, o);
                } else {
                    r = getInsertSql(tableName, fs, o);
                }
                if (r == null)
                    continue;
                for (String s : r.keySet()) {
                    sql.add(s);
                    param.add(r.get(s));
                }
            }
            // 如果有数据需要存储执行存储
            if (sql.size() > 0) {
                // 入库
                batchExecSQL(db, sql.toArray(new String[sql.size()]), param.toArray(new String[param.size()][]));
                result = true;
            }
        }
        return result;
    }

    /**
     * 将给定集合中的对象存储到db中，对象集合可以是不同的对象
     * （如果对象集合中包含对象是相同的，建议使用saveSameClassToDB(SQLiteDatabase,
     * Object...)方法，这样效率将更高）
     *
     * @param db  SQLiteDatabase
     * @param obj Object... 如果入参list,仅支持一个参数
     * @return true成功 false失败
     */
    @SafeVarargs
    public static synchronized <T extends BasicTable> boolean saveClassToDB(final SQLiteDatabase db, final T... obj) {
        boolean result = false;
        if (obj != null && obj.length > 0) {
            List<String> sql = new ArrayList<>();
            List<String[]> param = new ArrayList<>();
            for (Object o : obj) {
                Map<String, String[]> r = getInsertSql(o);
                if (r == null)
                    continue;
                for (String s : r.keySet()) {
                    sql.add(s);
                    param.add(r.get(s));
                }
            }
            // 如果有数据需要存储执行存储
            if (sql.size() > 0) {
                // 入库
                batchExecSQL(db, sql.toArray(new String[sql.size()]), param.toArray(new String[param.size()][]));
                result = true;
            }
        }
        return result;
    }

    /**
     * 批量执行SQL语句 bindArgs可以为null，如果不为null时必需保持与sql的数量相同（bindArgs的二维中允许为null）
     *
     * @param sql      sql[]
     * @param bindArgs Object[][]
     * @return true 处理成功
     */
    public static synchronized boolean batchExecSQL(final SQLiteDatabase db, final String[] sql, final Object[][] bindArgs) {
        boolean result = false;
        db.beginTransaction();
        try {
            int len = sql.length;
            try {
                if (bindArgs == null) {
                    for (String s : sql) {
                        db.execSQL(s);
                    }
                } else {
                    for (int i = 0; i < len; i++) {
                        if (bindArgs[i] == null) {
                            db.execSQL(sql[i]);
                        } else {
                            db.execSQL(sql[i], bindArgs[i]);
                        }
                    }
                }
                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // 提交事务
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return result;
    }
}
