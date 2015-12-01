package com.ldm.basic.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * Created by ldm on 14-12-15.
 * 根据提供当前时间及给定的格式，返回时间字符串
 */
public class CPUHelper {

    /**
     * 返回CPU核心数
     *
     * @return 读取失败时 返回1
     */
    public static int getNumCores() {
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]", pathname.getName());
                }
            });
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 返回CPU最大频率,显示模式0.5/1.0/1.2/1.5 ,换算KHZ 需要*1000000
     *
     * @return KHZ / 1000000
     */
    public static float getCpuMaxFreq() {
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            return TextUtils.parseLong(line, 1000000) / 1000000.0f;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 1.0f;
    }

    /**
     * 返回CPU的最小频率 显示模式0.5/1.0/1.2/1.5 ,换算KHZ 需要*1000000
     *
     * @return KHZ / 1000000
     */
    public static float getCpuMinFreq() {
        float result;
        ;
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            String data = "";
            while (in.read(re) != -1) {
                data = data + new String(re);
            }
            result = (TextUtils.parseLong(data.trim(), 1000000)) / 1000000f;
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            result = -1f;
        }
        return result;
    }

    /**
     * 返回CPU当前的频率 显示模式0.5/1.0/1.2/1.5 ,换算KHZ 需要*1000000
     *
     * @return KHZ / 1000000
     */
    public static float getCpuCurFreq() {
        float result;
        try {
            FileReader fr = new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            br.close();
            result = TextUtils.parseLong(text.trim(), 1000000) / 1000000f;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = -1;
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }
        return result;
    }
}
