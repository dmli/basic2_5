package com.ldm.basic.model;

import java.util.List;

import com.ldm.basic.db.BasicSQLiteOpenHelper;
import com.ldm.basic.db.BasicTable;
import com.ldm.basic.db.DBHelper;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by ldm on 14-5-30. 数据模型基础类
 */
public abstract class BasicDataModel {

	protected Context context;

	public BasicDataModel(Context context) {
		this.context = context;
	}

	/**
	 * 开发者需要重写该方法
	 *
	 * @param ct 表实体
	 * @param param 参数集合
	 * @param <T> <T extends BasicTable>
	 * @return List<T>
	 */
	public abstract <T extends BasicTable> List<T> query(final Class<T> ct, final String... param);

	/**
	 * 查询单条数据
	 *
	 * @param ct 表映射的实体
	 * @param where 条件
	 * @param param 参数
	 * @param <T> T extends BasicTable
	 * @return T
	 */
	public <T extends BasicTable> T queryOnly(final Class<T> ct, final String where, final String... param) {
		try {
			return DBHelper.queryOnlyToClass(context, ct, where, param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 查询单条数据
	 *
	 * @param ct 表映射的实体
	 * @param where 条件
	 * @param param 参数
	 * @param <T> T extends BasicTable
	 * @return T
	 */
	public <T extends BasicTable> T queryOnly(final String tableName, final String where, final Class<T> ct, final String... param) {
		try {
			return DBHelper.queryOnlyToClass(context, tableName, ct, where, param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 返回表总记录数
	 *
	 * @param ct 表实体
	 * @param <T> <T extends BasicTable>
	 * @return 记录数
	 */
	public <T extends BasicTable> int queryCount(final Class<T> ct) {
		return queryCount(ct, null, null, null);
	}

	/**
	 * 返回记录数
	 *
	 * @param ct 表实体
	 * @param where 条件
	 * @param param 参数集合
	 * @param <T> <T extends BasicTable>
	 * @return 记录数
	 */
	public <T extends BasicTable> int queryCount(final Class<T> ct, final String tableName, final String where, final String[] param) {
		int result = 0;
		BasicSQLiteOpenHelper db = BasicSQLiteOpenHelper.getInstance(context);
		Cursor c0;
		if (where == null || "".equals(where)) {
			c0 = db.rawQuery("select count(_id) as _id from " + (tableName == null ? BasicTable.getTableName(ct) : tableName), null);
		} else {
			c0 = db.rawQuery("select count(_id) as _id from " + (tableName == null ? BasicTable.getTableName(ct) : tableName) + " where " + where, param);
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
	 * 根据条件返回数据是否存在
	 *
	 * @param table 表明
	 * @param where 条件
	 * @param p 参数集合
	 * @return true存在
	 */
	public boolean isExist(final String table, final String where, final String... p) {
		boolean r = false;
		Cursor c0 = BasicSQLiteOpenHelper.getInstance(context).rawQuery("SELECT _id FROM " + table + " WHERE " + where, p);
		if (c0 != null) {
			if (c0.getCount() > 0) {
				r = true;
			}
			c0.close();
		}
		return r;
	}

	/**
	 * 将对象存储到数据库中
	 *
	 * @param o 需要存库的对象
	 * @param <T> T extends BasicTable
	 * @return true 保存成功
	 */
	public <T extends BasicTable> boolean save(T o) {
		return DBHelper.saveSameClassToDB(BasicSQLiteOpenHelper.getInstance(context).getWritableDatabase(), o);
	}

	/**
	 * 将对象存储到数据库中
	 *
	 * @param o 需要存库的对象
	 * @param <T> T extends BasicTable
	 * @return true 保存成功
	 */
	public <T extends BasicTable> boolean save(T o, String tableName) {
		return DBHelper.saveSameClassToDB(BasicSQLiteOpenHelper.getInstance(context).getWritableDatabase(), tableName, o);
	}

	/**
	 * 将对象列表存储到数据库中
	 *
	 * @param data 需要保存到库中的集合
	 * @param <T> <T extends BasicTable>
	 * @return true保存成功
	 */
	public <T extends BasicTable> boolean save(List<T> data) {
		return DBHelper.saveSameClassToDB(BasicSQLiteOpenHelper.getInstance(context).getWritableDatabase(), data.toArray(new BasicTable[data.size()]));
	}

	/**
	 * 开发者需要重写该方法
	 *
	 * @param data 需要保存到库中的集合
	 * @param table 表明
	 * @param <T> <T extends BasicTable>
	 * @return true保存成功
	 */
	public <T extends BasicTable> boolean save(List<T> data, final String table) {
		return DBHelper.saveSameClassToDB(BasicSQLiteOpenHelper.getInstance(context).getWritableDatabase(), table, data.toArray(new BasicTable[data.size()]));
	}

	/**
	 * 根据_id删除数据
	 *
	 * @param table 表名
	 * @param _id ID
	 * @return 是否删除成功， 如果表中没有找到记录返回false
	 */
	public boolean delete(final String table, final int _id) {
		BasicSQLiteOpenHelper db = BasicSQLiteOpenHelper.getInstance(context);
		return db.delete(table, "_id = ?", new String[] { _id + "" }) > 0;
	}

	/**
	 * 删除
	 *
	 * @param table 表名
	 * @param where 条件
	 * @param p 参数
	 * @return 是否删除成功， 如果表中没有找到记录返回false
	 */
	public boolean delete(final String table, final String where, String[] p) {
		BasicSQLiteOpenHelper db = BasicSQLiteOpenHelper.getInstance(context);
		return db.delete(table, where, p) > 0;
	}

	/**
	 * delete数据库第一条数据
	 * 
	 * @param table 数据表
	 */
	public void deleteFirstRow(final String table) {
		BasicSQLiteOpenHelper db = BasicSQLiteOpenHelper.getInstance(context);
		db.delete("delete from TABLE where _id = (select _id from TABLE Limit 0,1)", null);
	}

	/**
	 * 根据_id删除数据
	 *
	 * @param table 表名
	 * @return 是否删除成功
	 */
	public boolean deleteAll(final String table) {
		BasicSQLiteOpenHelper db = BasicSQLiteOpenHelper.getInstance(context);
		return db.delete(table, null, null) > 0;
	}

	/**
	 * 修改单条数据
	 *
	 * @param o 修改
	 * @param <T> T extends BasicTable
	 * @return true 修改成功
	 */
	public <T extends BasicTable> boolean update(T o) {
		return update(o, null);
	}

	/**
	 * 修改单条数据
	 *
	 * @param o 修改
	 * @param <T> T extends BasicTable
	 * @param tableName 表明
	 * @return true 修改成功
	 */
	public <T extends BasicTable> boolean update(T o, String tableName) {
		if (o.get_id() == -1) {
			return false;
		}
		return DBHelper.updateToClass(context, tableName, o);
	}

	/**
	 * 修改单条数据,如果不存在就改为保存
	 *
	 * @param o 修改
	 * @param <T> T extends BasicTable
	 * @return true 修改/保存 成功
	 */
	public <T extends BasicTable> boolean update2(T o) {
		return update2(o, null);
	}

	/**
	 * 修改单条数据,如果不存在就改为保存
	 *
	 * @param o 修改
	 * @param <T> T extends BasicTable
	 * @param tableName 表明
	 * @return true 修改/保存 成功
	 */
	public <T extends BasicTable> boolean update2(T o, String tableName) {
		if (o.get_id() <= -1) {
			return DBHelper.saveSameClassToDB(BasicSQLiteOpenHelper.getInstance(context).getWritableDatabase(), tableName, o);
		} else {
			return DBHelper.updateToClass(context, tableName, o);
		}
	}

}
