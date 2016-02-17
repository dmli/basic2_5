package com.ldm.basic.utils;

import com.ldm.basic.bean.ContactBean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * Created by ldm on 16/2/14.
 * 用来对数据Bean生成对应的表字段工具
 */
public class LFieldGenerateTool {

    public static void gen(Class<?> cla, String filePath) {
        Field[] fs = cla.getDeclaredFields();
        StringBuilder builder = new StringBuilder();
        for (Field fd : fs) {
            if (fd.getModifiers() == Modifier.PRIVATE || fd.getModifiers() == Modifier.PROTECTED) {
                builder.append("    public static final String COLUMN_");
                builder.append(fd.getName().toUpperCase(Locale.CHINA));
                builder.append(" = ");
                builder.append("\"");
                builder.append(fd.getName());
                builder.append("\";\n");
            }
        }

        try {
            StringBuilder oldFileData = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = br.readLine()) != null) {
                oldFileData.append(line).append("\n");
                if (builder != null) {
                    if (line.contains("{")) {
                        oldFileData.append("\n\n");
                        oldFileData.append("    /**\n");
                        oldFileData.append("     * 对应数据表字段\n");
                        oldFileData.append("    */\n");
                        oldFileData.append(builder);
                        builder = null;
                    }
                }
            }
            br.close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(oldFileData.toString(), 0, oldFileData.length());
            bw.close();

            System.out.println("数据表对应字段创建成功");

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {


        LFieldGenerateTool.gen(ContactBean.class,
                "/Users/ldm/basic_works/Basic2_5_1/Basic/src/main/java/com/ldm/basic/bean/ContactBean.java");

    }


}
