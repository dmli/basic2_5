package com.ldm.basic.db;

import java.io.Serializable;
import java.util.Locale;

import org.json.JSONObject;

/**
 * Created by ldm on 14-2-7. 数据表
 */
public class BasicTable implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 1L;

	/**
	 * 数据表默认ID
	 */
	@Column(type = Column.TYPE_INTEGER, notNull = true, primaryKey = true, autoIncrement = true)
	private int _id = -1;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getString(JSONObject obj, String key) {
		return obj.isNull(key) ? null : obj.optString(key);
	}

	public String getTableName() {
		return ((Object) this).getClass().getSimpleName().toUpperCase(Locale.CANADA);
	}

	public static String getTableName(Class<?> table) {
		return table.getSimpleName().toUpperCase(Locale.CANADA);
	}
}
