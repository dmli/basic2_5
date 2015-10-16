package com.ldm.basic.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author LDM  XmlTool.java 2012-11-2 下午9:48:18
 * xml生成工具
 */
public class XmlBuilder {
	
	private final static String XML_CHARSET = "UTF-8";
	private final static String XML_VERSION = "1.0";
	private final static String XML_LEFT_LABEL = "<";
	private final static String XML_RIGHT_LABEL = ">";
	private final static String XML_ENDING_LABEL = "/";
	
	private String root;
	private StringBuffer xml = null;
	private List<String> nodes;

	/**
	 * 实例化XML工具，版本1.0 编码 UTF-8
	 */
	public XmlBuilder(){
		newInstance(XML_VERSION, XML_CHARSET);
    }
	
	/**
	 * 实例化XML工具，版本1.0 
	 * @param charset XML字符编码 如：UTF-8
	 */
	public XmlBuilder(final String charset){
		newInstance(XML_VERSION, charset);
    }
	
	/**
	 * 实例化XML工具
	 * @param version 版本如：1.0
	 * @param charset 编码： UTF-8
	 */
	public XmlBuilder(final String version, final String charset){
        newInstance(version, charset);
    }
	
	/**
	 * 初始化实例
	 * @param version xml版本 默认1.0
	 * @param charset 字符编码
	 */
	private void newInstance(final String version, final String charset){
        xml = new StringBuffer();
		nodes = new ArrayList<String>();
		xml.append("<?xml version=\""+version+"\" encoding=\""+charset+"\"?>");
	}
	
	/**
	 * 增加根节点
	 * @param name 节点名
	 */
	public void addRoot(final String name){
		root = name;
		xml.append(XML_LEFT_LABEL + name + XML_RIGHT_LABEL);
	}
	
	/**
	 * 增加节点，节点有重复需要使用end结束
	 * @param name 节点名称
	 */
	public void addNode(final String name){
		nodes.add(name);
		xml.append(XML_LEFT_LABEL).append(name).append(XML_RIGHT_LABEL);
	}
	
	/**
	 * 添加节点内元素，且元素内没有子节点
	 * @param name 元素名称
	 * @param value 元素值
	 */
	public void addProperty(final String name, final String value){
		xml.append(XML_LEFT_LABEL).append(name).append(XML_RIGHT_LABEL).append(value)
		.append(XML_LEFT_LABEL).append(XML_ENDING_LABEL).append(name).append(XML_RIGHT_LABEL);
	}
	
	public void addProperty(final String name, final String value, final Map<String, String> atts){
		xml.append(XML_LEFT_LABEL).append(name).append(XML_RIGHT_LABEL).append(value).append(XML_LEFT_LABEL).append(XML_ENDING_LABEL).append(name).append(XML_RIGHT_LABEL);
	}
	
	/**
	 * 二级节点结束后需要调用，在节点内有循环时调用
	 */
	public void end(){
		for (int i = nodes.size() - 1; i >= 0; i--) {
			xml.append(XML_LEFT_LABEL + XML_ENDING_LABEL + nodes.remove(i) + XML_RIGHT_LABEL);
		}
	}
	
	/**
	 * 获得组装的XML文本格式
	 * @return XML字符串
	 */
	public String toXml(){
		xml.append(XML_LEFT_LABEL + XML_ENDING_LABEL + root + XML_RIGHT_LABEL);
		release();
		return xml.toString();
	}
	
	/**
	 * 释放所有资源
	 */
	public void release(){
		root = null;
		xml = null;
		if(nodes != null){
			nodes.clear();
			nodes = null;
		}
	}
	
	/**
	 * 使用方法
	 * @param args a
	 */
	public static void main(String[] args) {
		XmlBuilder x = new XmlBuilder();
		x.addRoot("node");
		x.addNode("n1");
		x.addProperty("a1", "1111");
		x.addProperty("a2", "2222");
		x.addProperty("a3", "3333");
		x.addProperty("a4", "4444");
		x.end();
		x.addNode("n2");
		x.addProperty("a1", "1111");
		x.addProperty("a2", "2222");
		x.addProperty("a3", "3333");
		x.addProperty("a4", "4444");
		x.addNode("n2n2");
		x.addProperty("aa1", "1111");
		x.addProperty("aa2", "1111");
		x.addProperty("aa3", "1111");
		x.addProperty("aa4", "1111");
		x.end();
		System.out.println(x.toXml());
	}
}
