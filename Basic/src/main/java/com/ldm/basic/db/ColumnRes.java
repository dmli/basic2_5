package com.ldm.basic.db;

/**
 * Created by ldm on 14-4-10.
 * 表字段属性，用来根据类反射表时使用
 */
public class ColumnRes {

	private String name;// 字段名
	private String type;// 类型
	private String defaultValue;
	private boolean notNull;// 是否可以为空 false可以为空
	private boolean primaryKey;// 是否主键 true主键
	private boolean autoIncrement;// 是否自动增长 true自动增长

	/**
	 * 构造一个ColumnRes
	 *
	 * @param name 字段名
	 * @param type 类型
	 * @param notNull 是否可以为空 false可以为空
	 * @param primaryKey 是否主键 true主键
	 * @param autoIncrement 是否自动增长 true自动增长
	 */
	public ColumnRes(String name, String type, boolean notNull, boolean primaryKey, boolean autoIncrement) {
		this.name = name;
		this.type = type;
		this.notNull = notNull;
		this.primaryKey = primaryKey;
		this.autoIncrement = autoIncrement;
	}

	/**
	 * 构造一个ColumnRes
	 *
	 * @param name 字段名
	 * @param type 类型
	 * @param notNull 是否可以为空 false可以为空
	 * @param primaryKey 是否主键 true主键
	 * @param autoIncrement 是否自动增长 true自动增长
	 * @param defaultValue 默认值
	 */
	public ColumnRes(String name, String type, boolean notNull, boolean primaryKey, boolean autoIncrement, String defaultValue) {
		this.name = name;
		this.type = type;
		this.notNull = notNull;
		this.primaryKey = primaryKey;
		this.autoIncrement = autoIncrement;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
