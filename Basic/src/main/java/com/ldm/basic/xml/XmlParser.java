package com.ldm.basic.xml;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldm on 12-11-2.
 * xml解析
 */
public class XmlParser {

    /**
     * 解析XML，返回对应的list结果集，此方法会过滤掉XML标签内的所有属性
     *
     * @param reader    xml流
     * @param clazz     xml对应的实体
     * @param startName 开始位置
     * @return List列表
     */
    public static List<Object> getXmlList(Reader reader, Class<?> clazz, String startName) {
        List<Object> list = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(reader);
            list = xmlToList(parser, clazz, startName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 解析XML，返回对应的list结果集
     *
     * @param is        xml流
     * @param charset   编码
     * @param clazz     xml对应的实体
     * @param startName 开始位置
     * @return List列表
     */
    public static List<Object> getXmlList(InputStream is, String charset, Class<?> clazz, String startName) {
        List<Object> list = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(is, charset);
            list = xmlToList(parser, clazz, startName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 解析XML，返回对应的Object
     *
     * @param reader    xml流
     * @param clazz     xml对应的实体
     * @param startName 开始位置
     * @return Object
     */
    public static Object getXmlObject(Reader reader, Class<?> clazz, String startName) {
        Object result = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(reader);
            result = xmlToObject(parser, clazz, startName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 解析XML，返回对应的Object
     *
     * @param is        xml流
     * @param charset   编码
     * @param clazz     xml对应的实体
     * @param startName 开始位置
     * @return Object
     */
    public static Object getXmlObject(InputStream is, String charset, Class<?> clazz, String startName) {
        Object result = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(is, charset);
            result = xmlToObject(parser, clazz, startName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从某节点开始，将所有的子元素的（不包含属性）[TEXT]赋给与[TAG]名相同的[对象]属性中
     *
     * @param parser    XmlPullParser
     * @param clazz     xml对应的实体
     * @param startName 开始的标签名称
     * @return 结果集
     * @throws org.xmlpull.v1.XmlPullParserException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws java.io.IOException
     */
    private static List<Object> xmlToList(XmlPullParser parser, Class<?> clazz, String startName)
            throws XmlPullParserException, InstantiationException, IllegalAccessException, IOException {
        List<Object> list = null;
        int eventType = parser.getEventType();// 当前读取类型标记
        Object object = null;
        boolean isAdd = false;// 是否开始向Object中填充数据
        String _name = null; // 节点名称
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    list = new ArrayList<>();
                    break;
                case XmlPullParser.START_TAG:
                    _name = parser.getName();
                    if (startName.equals(_name)) {
                        isAdd = true;
                        object = clazz.newInstance();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (startName.equals(parser.getName())) {
                        if (list != null) {
                            list.add(object);
                        }
                        isAdd = false;
                        object = null;
                    }
                    break;
                case XmlPullParser.TEXT:
                    if (isAdd) {
                        setXmlValue(object, _name, parser.getText());
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return list;
    }

    /**
     * @param parser    XmlPullParser
     * @param clazz     xml对应的实体
     * @param startName 开始的标签名称
     * @return Object
     * @throws org.xmlpull.v1.XmlPullParserException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws java.io.IOException
     */
    private static Object xmlToObject(final XmlPullParser parser, final Class<?> clazz, final String startName)
            throws XmlPullParserException, InstantiationException, IllegalAccessException, IOException {
        Object object = null;
        boolean isAdd = false;// 是否开始向Object中填充数据
        String _name = null; // 节点名称

        int eventType = parser.getEventType();// 当前读取类型标记
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    _name = parser.getName();
                    if (startName.equals(_name)) {
                        isAdd = true;
                        object = clazz.newInstance();
                    }
                    break;
                case XmlPullParser.TEXT:
                    if (isAdd) {
                        setXmlValue(object, _name, parser.getText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (startName.equals(parser.getName())) {
                        isAdd = false;
                        break;//终止循环
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }
        return object;
    }

    /**
     * 把xml标签的值，转换成对象里属性的值
     *
     * @param t     对象
     * @param name  xml标签名
     * @param value xml标签名对应的值
     */
    private static void setXmlValue(Object t, String name, String value) {
        try {
            Field[] f = t.getClass().getDeclaredFields();
            for (Field field : f) {
                if (field.getName().equalsIgnoreCase(name)) {
                    field.setAccessible(true);
                    // 获得属性类型
                    Class<?> fieldType = field.getType();

                    if (fieldType == String.class) {
                        field.set(t, value);
                    } else if (fieldType == Integer.TYPE) {
                        field.set(t, Integer.parseInt(value));
                    } else if (fieldType == Float.TYPE) {
                        field.set(t, Float.parseFloat(value));
                    } else if (fieldType == Double.TYPE) {
                        field.set(t, Double.parseDouble(value));
                    } else if (fieldType == Long.TYPE) {
                        field.set(t, Long.parseLong(value));
                    } else if (fieldType == Short.TYPE) {
                        field.set(t, Short.parseShort(value));
                    } else if (fieldType == Boolean.TYPE) {
                        field.set(t, Boolean.parseBoolean(value));
                    } else {
                        field.set(t, value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}