package com.ldm.basic.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by ldm on 13-12-14. 常用的File操作代码段
 */
public class FileTool {

    /**
     * 将数据流转存为本地文件
     *
     * @param is        数据流
     * @param path      转存地址
     * @param cacheName 名称（需要带后缀）
     * @return 如果保存成功将返回全路径，失败返回null
     * @throws IOException
     */
    public static String save(InputStream is, final String path, final String cacheName) throws IOException {
        return save(is, path + "/" + cacheName);
    }

    /**
     * 将数据流转存为本地文件
     *
     * @param is       数据流
     * @param filePath 文件绝对地址
     * @return 如果保存成功将返回全路径，失败返回null
     * @throws IOException
     */
    public static String save(InputStream is, final String filePath) throws IOException {
        // 测试此缓存目录是否存在
        File f = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        boolean bool = true;
        if (!f.isDirectory()) {
            bool = f.mkdirs();
        }
        if (bool) {
            // 写入
            write(is, new FileOutputStream(filePath));
        } else {
            throw new IOException("f.mkdirs() return false");
        }
        return filePath;
    }

    /**
     * 将字符串写入到指定文件中，如果文件不存在就创建一个
     *
     * @param data      数据
     * @param path      目录
     * @param cacheName 名称
     * @return 如果成功将返回全地址
     * @throws IOException
     */
    public static String save(final String data, final String path, final String cacheName) throws IOException {
        return save(data, path + "/" + cacheName);
    }

    /**
     * 将字符串写入到指定文件中，如果文件不存在就创建一个
     *
     * @param data     数据
     * @param filePath 文件绝对地址
     * @return 如果成功将返回全地址
     * @throws IOException
     */
    public static String save(final String data, final String filePath) throws IOException {
        // 测试此缓存目录是否存在
        File f = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        boolean bool = true;
        if (!f.isDirectory()) {
            bool = f.mkdirs();
        }
        if (bool) {
            // 写入
            write(data, new FileOutputStream(filePath));
        } else {
            throw new IOException("f.mkdirs() return false");
        }
        return filePath;
    }

    /**
     * 将指定文件转换成InputStream
     *
     * @param path 路径
     * @return InputStream
     */
    public static InputStream openFile(final String path) {
        return openFile(new File(path));
    }

    /**
     * 将指定文件中的内容以行的方式整理成List<String>后返回
     *
     * @param f0 文件
     * @return List<String>
     */
    public static List<String> fileToLines(File f0) {
        return inputStreamToLines(openFile(f0));
    }

    /**
     * 将指定路径指向的文件中的内容以行的方式整理成List<String>后返回
     *
     * @param filePath 文件地址
     * @return List<String>
     */
    public static List<String> fileToLines(final String filePath) {
        return inputStreamToLines(openFile(filePath));
    }

    /**
     * 将指定InputStream中的内容以行的方式整理成List<String>后返回
     *
     * @param is InputStream
     * @return List<String>
     */
    public static List<String> inputStreamToLines(InputStream is) {
        List<String> result = null;
        if (is != null) {
            result = new ArrayList<>();
            InputStreamReader inReader = new InputStreamReader(is);
            BufferedReader buffReader = new BufferedReader(inReader);
            String data;
            try {
                while ((data = buffReader.readLine()) != null) {
                    if (!"".equals(data.trim())) {
                        result.add(data.trim());
                    }
                }
                is.close();
                inReader.close();
                buffReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 将指定文件转换成InputStream
     *
     * @param file 文件
     * @return InputStream
     */
    public static InputStream openFile(File file) {
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 转存，将InputStream的内容写到OutputStream中，写完后InputStream/OutputStream将自动关闭
     *
     * @param is  InputStream
     * @param out OutputStream
     * @throws IOException
     */
    public static void write(InputStream is, OutputStream out) throws IOException {
        int len;
        byte[] bytes = new byte[4096];
        while ((len = is.read(bytes)) != -1) {
            out.write(bytes, 0, len);
        }
        out.flush();
        out.close();
        is.close();
    }

    /**
     * 将字符串以UTF-8编码写到OutputStream中
     *
     * @param data 数据
     * @param out  OutputStream
     * @throws IOException
     */
    public static void write(final String data, OutputStream out) throws IOException {
        out.write(data.getBytes(), 0, data.getBytes().length);
    }

    /**
     * inputStream转byte
     *
     * @param inStream InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] input2byte(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1020];
        int rc;
        while ((rc = inStream.read(buff, 0, buff.length)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        return swapStream.toByteArray();
    }

    /**
     * 创建指定路径所需要的所有文件夹
     *
     * @return true成功
     */
    public static boolean createDirectory(final String path) {
        File f0 = new File(path);
        boolean exists = true;
        if (!f0.isDirectory()) {
            exists = f0.mkdirs();
        }
        return exists;
    }

    /**
     * 删除path指定的文件
     *
     * @param path 路径
     * @return true删除成功
     */
    public static boolean delete(final String path) {
        return path != null && delete(new File(path));
    }

    /**
     * 获取指定文件夹内所有文件的空间占用大小,递归查询，建议异步使用
     *
     * @param f 文件
     * @return long bety
     */
    public static long getDirectoryLength(final File f) {
        long length = 0;
        if (f.exists()) {
            if (f.isDirectory()) {
                File[] fs = f.listFiles();
                if (fs != null) {
                    for (File f0 : fs) {
                        if (f0.isDirectory()) {
                            length += getDirectoryLength(f0);
                        } else {
                            length += f0.length();
                        }
                    }
                }
            } else {
                length += f.length();
            }
        }
        return length;
    }

    /**
     * 返回指定目录下的文件列表
     *
     * @param directory 指定目录路径
     * @param recursive 如果遇到目录是否进行迭代 true迭代
     * @return List<String>
     */
    public static List<String> getFilePaths(final File directory, boolean recursive) {
        if (!directory.isDirectory())
            return null;
        List<String> result = new ArrayList<>();
        File[] fs = directory.listFiles();
        if (fs == null)
            return null;
        for (File f : fs) {
            if (f.isFile()) {
                result.add(f.getAbsolutePath());
            } else if (f.isDirectory() && recursive) {
                List<String> r = getFilePaths(f, true);
                if (r != null) {
                    result.addAll(r);
                }
            }
        }
        return result;
    }

    /**
     * 删除File文件
     *
     * @param file 需要删除的文件
     * @return true删除成功
     */
    public static boolean delete(File file) {
        return file == null || !file.exists() || file.delete();
    }

    /**
     * 如果指定path是文件夹则执行递归删除文件夹下的所有文件及子文件夹
     *
     * @param path 文件或文件夹路径
     */
    public static void deleteAllFiles(final String path) {
        File f = new File(path);
        if (f.exists()) {
            deleteAllFiles(f);
        }
    }

    /**
     * 如果指定path是文件夹则执行递归删除文件夹下的所有文件及子文件夹
     *
     * @param path 文件File
     */
    public static boolean deleteAllFiles(File path) {
        if (!path.exists())
            return false;
        if (path.isFile()) {
            return path.delete();
        }
        File[] files = path.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                deleteAllFiles(file);
            }
        }
        return path.delete();
    }

    /**
     * 执行linux rm命令删除指定目录或文件
     *
     * @param path 文件或文件夹路径
     */
    public static boolean deleteToLinux(final String path) {
        boolean result = false;
        try {
            Runtime.getRuntime().exec("rm -rf " + path);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将inputStream转成String后返回
     *
     * @param is 数据流
     * @return String
     */
    public String inputStreamToString(InputStream is) {
        if (is == null) {
            return null;
        }
        String bRet = null;
        InputStreamReader inReader = new InputStreamReader(is);
        BufferedReader buffReader = new BufferedReader(inReader);
        try {
            String d;
            StringBuilder data = new StringBuilder();
            while ((d = buffReader.readLine()) != null) {
                if (!"".equals(d.trim())) {
                    data.append(d);
                }
            }
            inReader.close();
            buffReader.close();

            bRet = data.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inReader.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            try {
                buffReader.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return bRet;
    }

    /**
     * 获取文件的MD5值
     *
     * @param filePath File地址
     * @return null获取失败
     */
    public static String getFileMD5(final String filePath) {
        return getFileMD5(new File(filePath));
    }

    /**
     * 获取文件的MD5值
     *
     * @param file File
     * @return null获取失败
     */
    public static String getFileMD5(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 解压ZIP
     *
     * @param zipPath    ZIP文件路径
     * @param unpackPath 将要释放到的路径
     * @return true加载完成
     */
    public boolean zipUnpack(String zipPath, String unpackPath) {
        boolean result = false;
        ZipFile skinZip = null;
        try {
            if (FileTool.createDirectory(unpackPath)) {
                skinZip = new ZipFile(zipPath);
                for (Enumeration<? extends ZipEntry> entries = skinZip.entries(); entries.hasMoreElements(); ) {
                    ZipEntry z = entries.nextElement();
                    if (z.isDirectory()) {
                        FileTool.createDirectory(unpackPath + "/" + z.getName());
                    } else {
                        File outFile = new File(unpackPath + "/" + z.getName());
                        if (outFile.exists()) {
                            delete(outFile);
                        }
                        if (outFile.createNewFile()) {
                            write(skinZip.getInputStream(z), new FileOutputStream(outFile));
                        }
                    }
                }
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (skinZip != null) {
                try {
                    skinZip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
