package com.ldm.basic.db;

/**
 * Created by ldm on 15/12/2.
 * 数据库配置信息
 */
public abstract class DBConfig {

    /**
     * 数据库名称
     *
     * @return str
     */
    public abstract String getDbName();

    /**
     * 数据库版本
     *
     * @return int
     */
    public abstract int getDbVersion();

    /**
     * 数据库回掉包名
     *
     * @return str
     */
    public abstract String getDbCallbackPackage();

}
