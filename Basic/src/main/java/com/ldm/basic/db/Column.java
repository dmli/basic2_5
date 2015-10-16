package com.ldm.basic.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ldm on 14-4-10. 数据库列注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    static final String _DEFAULT_VALUE = "#NULL#";

    /**
     * varchar
     */
    public static final String TYPE_CAR_CHAR = "varchar";

    /**
     * integer
     */
    public static final String TYPE_INTEGER = "integer";

    /**
     * float
     */
    public static final String TYPE_LONG = "float";

    /**
     * double
     */
    public static final String TYPE_DOUBLE = "double";

    /**
     * 是否忽略_id自动增长主键
     *
     * @return true忽略
     */
    public boolean ignoreAutoPrimaryKey() default false;

    /**
     * 字段类型，默认varchar
     *
     * @return 字符串形式的类型
     */
    public String type() default TYPE_CAR_CHAR;

    /**
     * 是否数据库主键，默认false
     *
     * @return true主键
     */
    public boolean primaryKey() default false;

    /**
     * 是否可以为空 默认false
     *
     * @return false可以为空
     */
    public boolean notNull() default false;

    /**
     * 是否自动增长 默认false
     *
     * @return true自动增长
     */
    public boolean autoIncrement() default false;

    public String defaultValue() default _DEFAULT_VALUE;
}
