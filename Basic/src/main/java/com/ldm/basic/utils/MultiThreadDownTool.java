package com.ldm.basic.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.JsonParseException;
import com.ldm.basic.bean.FileDownloadRecord;
import com.ldm.basic.bean.MultiThreadTaskRef;

import android.os.Handler;
import android.os.Message;

/**
 * Created by ldm on 12-11-15.
 * 多线程下载工具（支持断点续传），工具内设置了一个界限 MIN_LENGTH ，如果网络文件小于该值的话则执行单线程下载
 * 不支持同时多次start,当isIdle等于true时可以在此执行start
 */
public class MultiThreadDownTool {

    private static final int PROGRESS_CALLBACK_SUCCESS = 170;
    private static final int PROGRESS_CALLBACK_PROGRESS = 171;
    private static final int PROGRESS_CALLBACK_ERROR = 172;
    private static final int PROGRESS_CALLBACK_IO_ERROR = 173;
    private static final int PROGRESS_CALLBACK_STAGES_TO_COMPLETE = 175;//阶段性完成

    private static final String MODE = "rwd";//可选模式：rw、rws、rwd
    private static final int MIN_LENGTH = 1024 * 1024 * 3;//3m 如果文件小于MIN_LENGTH大小默认使用单线程下载

    private long fileSize; //文件总大小
    private long currentSize;//当前下载大小
    private int current; //当前完成线程数
    private String filePath;//文件的完成路径，包含名称
    private String fileName;
    private ProgressCallback progress;
    private FileDownloadRecord fdr;//下载情况
    private Download[] downloads;
    private boolean isIdle;//当内部任务执行完成后该状态为true空闲状态，可以在次使用start进行初始化
    private int stage;//阶段性备份计数器
    private long currentNumber;//阶段性备份累加器

    /**
     * 下载记得的缓存文件后缀名
     */
    public static final String DOWNLOAD_RECORD_SUFFIX = ".fdr";

    /**
     * 创建一个MultiThreadDownTool
     */
    public MultiThreadDownTool() {
        isIdle = true;//设置空闲状态
    }

    /**
     * 检测本地文件是否下载完成（每一个使用MultiThreadDownTool下载的文件使用前都需要验证）
     *
     * @param filePath 文件绝对路径 （localPath+fileName 与MultiThreadDownTool中的filePath含义相同）
     * @return 如果有配置信息返回该文件
     */
    public static FileDownloadRecord checkFileDownloadComplete(String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            //如果文件存在，检测下载记录
            File f0 = new File(filePath + DOWNLOAD_RECORD_SUFFIX);
            if (f0.exists()) {
                FileTool ft = new FileTool();
                try {
                    FileDownloadRecord data = SystemTool.getGson().fromJson(ft.inputStreamToString(ft.openFile(f0)), FileDownloadRecord.class);
                    //检测数据完整性
                    if (data != null
                            && data.getFileSize() > 0
                            && data.getMultiThreadTaskRef() != null
                            && data.getMultiThreadTaskRef().size() > 0
                            && data.getThreadTotal() > 0
                            && data.getUrl() != null
                            && data.getCurrentSize() > 0) {
                        return data;
                    }
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * @param filePath 文件地址
     * @return true文件有效
     */
    public static boolean isFileDownloadComplete(final String filePath) {
        FileDownloadRecord fdr = checkFileDownloadComplete(filePath);
        return fdr != null && fdr.isComplete();
    }

    /**
     * 执行下载操作
     *
     * @param url       http地址
     * @param localPath 需要保存到的路径
     * @param fileName  名称
     * @param progress  回调函数
     */
    public void start(final String url, final String localPath, final String fileName, final ProgressCallback progress, final int threadCount) {
        this.isIdle = false;
        this.filePath = localPath + File.separatorChar + fileName;
        this.fileName = fileName;
        this.progress = progress;
        //初始化
        if (reset(localPath)) {
            stopTask();
            return;
        }
        //启动线程 首先会分析任务状态，如果已有任务就使用 没有就创建新的任务
        new TaskAnalyseThread(url, fileName, progress, threadCount).start();
    }

    /**
     * 重置相关属性，每次start时需要调用，防止重用该工具时数据出错
     *
     * @param localPath 本地地址
     * @return true初始化成功
     */
    private boolean reset(String localPath) {
        this.currentSize = 0;
        this.current = 0;//初始化
        this.currentNumber = 0;
        this.fileSize = 0; //文件总大小
        this.fdr = null;//下载情况
        this.downloads = null;
        this.stage = 0;//阶段性备份计数器
        //保存相关信息
        if (!FileTool.createDirectory(localPath)) {
            if (progress != null) {
                progress.error("缓存目录创建失败,请检查SD卡使用权限！");
            }
            return true;
        }
        return false;
    }


    /**
     * 根据FileDownloadRecord配置描述的信息进行初始化下载任务
     */
    private void startForFdr(String newUrl) {
        FileDownloadRecord fdr = this.fdr;
        fileSize = fdr.getFileSize();
        currentSize = fdr.getCurrentSize() < 0 ? 0 : fdr.getCurrentSize();//模糊的进度，不精确的
        current = fdr.getCurrent();
        //设置新URL
        List<MultiThreadTaskRef> ttr = fdr.getMultiThreadTaskRef();
        for (MultiThreadTaskRef mm : ttr) {
            mm.setUrl(newUrl);
        }
        downloads = new Download[fdr.getThreadTotal()];
        createThreadForTaskRef(ttr);
    }

    /**
     * 创建线程任务
     *
     * @param singlesSize 单个线程下载大小
     */
    private void createThread(long singlesSize, String url) {
        List<MultiThreadTaskRef> records = new ArrayList<MultiThreadTaskRef>();
        for (int i = 0; i < getThreadTotal(); i++) {
            long end = (i + 1) * singlesSize;
            if (i == fdr.getThreadTotal() - 1 && fileSize % fdr.getThreadTotal() > 0) {
                end += fileSize % fdr.getThreadTotal();//最后一个线程负责下载所有剩余
            }
            records.add(new MultiThreadTaskRef(url, i, i * singlesSize, end));
        }
        //创建并启动线程
        createThreadForTaskRef(records);
        //保留任务数据
        fdr.setMultiThreadTaskRef(records);
    }

    /**
     * 根据List<TaskRef>列表创建并启动线程
     *
     * @param ts List<TaskRef> 任务列表
     */
    private void createThreadForTaskRef(List<MultiThreadTaskRef> ts) {
        for (MultiThreadTaskRef t : ts) {
            new MyThread(t).start();
        }
    }

    /**
     * 线程全部完成后将会调用ProgressCallback的success方法通知用户
     *
     * @param path  下载后的路径
     * @param index 当前完成线程索引， 从0开始
     */
    private synchronized void stagesToComplete(final String path, final int index) {
        if (progress != null) {
            try {
                progress.stagesToComplete(fdr.getThreadTotal(), index);
                downloads[index] = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //----------------记录线程完成状态------------------
        if (fdr.getMultiThreadTaskRef() != null && fdr.getMultiThreadTaskRef().size() > 0 && index < fdr.getMultiThreadTaskRef().size()) {
            if (fdr.getMultiThreadTaskRef().get(index) != null) {
                fdr.getMultiThreadTaskRef().get(index).setComplete(true);
            }
        }
        saveFdr();//保存当前进度

        //----------------检查是否整体下载完成----------------
        current++;//该值必需在下载完成时保存，所以不能在saveFdr()之前执行该行代码
        fdr.setCurrent(current);
        if (current == fdr.getThreadTotal()) {
            //记录完成状态
            if (fdr != null) {
                fdr.setComplete(true);
            }
            if (progress == null) {
                //当全部执行完毕后触发停止操作
                stopTask();
            } else {
                //在线程中执行完asynchronous回调
                new Thread() {
                    @Override
                    public void run() {
                        securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_SUCCESS, progress.asynchronous(path)));
                    }
                }.start();
            }
        }
    }

    /**
     * 整体下载完成
     *
     * @param obj asynchronous返回的参数
     */
    private void success(String obj) {
        if (progress != null) {
            progress.success(obj);
        }
        //当全部执行完毕后触发停止操作
        stopTask();
    }

    /**
     * 时时进度更新
     *
     * @param index 线程索引
     * @param curr  当前下载值
     */
    private synchronized void progress(int index, int curr) {
        this.currentSize += curr;
        //阶段性存储下载日志
        currentNumber += curr;
        if (currentNumber > stage * 1048567) {//累计下载1M后开始存储
            saveFdr();
            stage++;
        }
        if (progress != null) {
            progress.progress(index, fileSize, currentSize);
        }
        MultiThreadTaskRef ref = null;
        if (fdr.getMultiThreadTaskRef() != null && fdr.getMultiThreadTaskRef().size() > 0 && index < fdr.getMultiThreadTaskRef().size()) {
            ref = fdr.getMultiThreadTaskRef().get(index);
        }
        if (ref != null) {
            ref.setCompleteSize(ref.getCompleteSize() + curr);
        } else {
            error(null);
        }
    }

    /**
     * 验证索引对应的线程是否处于完成状态
     *
     * @param index 索引 取值范围 [0 到 (getThreadTotal() - 1)] 超出索引将返回true
     * @return true线程运行结束
     */
    public boolean isThreadComplete(int index) {
        return (index < 0 || index > getThreadTotal() - 1) || (fdr != null && fdr.getMultiThreadTaskRef().size() > 0 && index < fdr.getMultiThreadTaskRef().size() && fdr.getMultiThreadTaskRef().get(index).isComplete());
    }

    /**
     * 停止所有任务
     */
    public void stopTask() {
        //停止所有本次使用的下载工具
        if (downloads != null && downloads.length > 0) {
            for (Download download : downloads) {
                if (download != null) {
                    download.isStop = true;
                }
            }
        }
        //当任务停止时 不考虑任何情况，只要停止就保存当前下载记录
        saveFdr();
        isIdle = true;
    }

    /**
     * 返回是否处于空闲状态
     *
     * @return true空闲状态
     */
    public boolean isIdle() {
        return isIdle;
    }

    private int errIndex = -1;

    /**
     * 文件下载失败，
     * 如果先内任意一个线程调用了该方法，则本地下载停止
     */
    private synchronized void error(MultiThreadTaskRef ref) {
        //当满足以下条件时尝试重新下载
        if (ref != null && errIndex != ref.getIndex()) {
            errIndex = ref.getIndex();
            //使用新的线程重新创建TaskRef
            MyThread newThread = new MyThread(ref);
            newThread.start();
            return;
        }
        //-------------------提示用户下载失败---------------------
        stopTask();

        //保存当前下载记录，方便后期继续下载
        saveFdr();

        //通知用户下载失败
        if (progress != null) {
            progress.error("下载失败");
        }

        /**
         * 下载失败删除文件
         */
        File f = new File(filePath);
        if (f.exists()) {
            if (f.delete()) {
                Log.e(MultiThreadDownTool.class.getSimpleName(), " new File(filePath).delete() return false");
            }
        }
    }

    /**
     * 保存当前下载进度记录
     * 方法保存 全局 fdr 变量
     */
    private void saveFdr() {
        if (fdr != null) {
            //本地缓存下载配置信息
            FileTool ft = new FileTool();
            fdr.setCurrentSize(currentSize);//不是十分精确的进度
            try {
                ft.save(SystemTool.getGson().toJson(fdr), filePath + DOWNLOAD_RECORD_SUFFIX);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回文件地址
     *
     * @return filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 获取本次线程总数
     *
     * @return threadTotal
     */
    public int getThreadTotal() {
        return fdr == null ? 0 : fdr.getThreadTotal();
    }

    /**
     * 获取文件名
     *
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }

    public FileDownloadRecord getFdr() {
        return fdr;
    }

    class MyThread extends Thread {
        public boolean isDone;
        public Download download;
        public MultiThreadTaskRef ref;

        MyThread(MultiThreadTaskRef ref) {
            this.ref = ref;
            this.isDone = false;
        }

        @Override
        public void run() {
            try {
                HttpClient c = new DefaultHttpClient();
                HttpGet get = new HttpGet(ref.getUrl());
                get.addHeader("Range", "bytes=" + (ref.getStartPosition() + ref.getCompleteSize()) + "-" + ref.getEndPosition());//设置要下载该文件的起始位置
                HttpResponse response = c.execute(get);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200 || statusCode == 206) {
                    HttpEntity entity = response.getEntity();
                    download = new Download(entity.getContent(), (ref.getStartPosition() + ref.getCompleteSize()), ref.getIndex());
                    downloads[ref.getIndex()] = download;
                    if (!download.down()) {
                        //下载失败
                        securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_ERROR, new MultiThreadTaskRef(ref)));
                    } else {
                        //下载成功
                        securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_STAGES_TO_COMPLETE, ref.getIndex(), 0, filePath));
                    }
                    isDone = true;
                } else {
                    //下载失败
                    securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_ERROR, new MultiThreadTaskRef(ref)));
                }
            } catch (IOException e) {
                //下载失败
                securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_ERROR, new MultiThreadTaskRef(ref)));
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载类
     */
    class Download {
        InputStream is;
        long startIndex;
        int index;//线程索引
        boolean isStop;//用户强行停止时，该变量为true

        Download(InputStream is, long startIndex, int index) {
            this.is = is;
            this.startIndex = startIndex;
            this.index = index;
        }

        /**
         * 在inputStream中读取数据，从startIndex开始到endIndex结束
         */
        public boolean down() {
            boolean result = false;//返回true表示正常下载完成
            if (is != null) {
                InputStream is = this.is;
                RandomAccessFile raf = null;
                try {
                    raf = new RandomAccessFile(filePath, MODE);
                    raf.seek(startIndex);//设置偏移量
                    int numRead;
                    byte[] bys = new byte[8192];
                    while ((numRead = is.read(bys)) != -1) {
                        if (isStop) {// 如果用户取消，使用异常终止下载
                            break;
                        }
                        if (numRead > 0) {
                            raf.write(bys, 0, numRead);
                            securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_PROGRESS, index, numRead));
                        }
                    }
                    result = !isStop;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                        if (raf != null) {
                            raf.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }
    }

    /**
     * 回调接口，该接口内所有方法均为异步操作，如果需要更新UI，请使用Handler配合
     */
    public static abstract class ProgressCallback {

        /**
         * 用户可以设置一个标记，多任务时用来区分任务
         */
        public String tag;

        protected ProgressCallback() {
        }

        protected ProgressCallback(String tag) {
            this.tag = tag;
        }

        /**
         * 线程安全的
         *
         * @param index   线程索引
         * @param count   总大小
         * @param current 当前大小
         */
        public void progress(int index, long count, long current) {
        }

        /**
         * 阶段性完成回调
         *
         * @param tCount 线程总数
         * @param index  当前完成线程索引， 从0开始
         */
        public void stagesToComplete(int tCount, int index) {

        }

        /**
         * 文件下载完成后的回调
         *
         * @param path 文件路径
         */
        public abstract void success(String path);

        /**
         * 该函数为异步函数，将在success前执行
         *
         * @param path 地址集合
         */
        public String asynchronous(String path) {
            return path;
        }

        /**
         * 文件下载失败
         *
         * @param error 错误信息
         */
        public abstract void error(String error);

        /**
         * IO异常
         *
         * @param error 错误信息
         */
        public void ioError(String error) {
        }
    }

    /**
     * 配合下载线程更新进度相关信息
     */
    private SecurityHandler securityHandler = new SecurityHandler(this);

    private static class SecurityHandler extends Handler {
        WeakReference<MultiThreadDownTool> mtd;

        private SecurityHandler(MultiThreadDownTool multiThreadDownTool) {
            this.mtd = new WeakReference<MultiThreadDownTool>(multiThreadDownTool);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mtd == null || mtd.get() == null) return;
            MultiThreadDownTool m = mtd.get();
            switch (msg.what) {
                case PROGRESS_CALLBACK_SUCCESS:
                    if (m != null) {
                        m.success(String.valueOf(msg.obj));
                    }
                    break;
                case PROGRESS_CALLBACK_STAGES_TO_COMPLETE:
                    if (m != null) {
                        m.stagesToComplete(String.valueOf(msg.obj), msg.arg1);
                    }
                    break;
                case PROGRESS_CALLBACK_PROGRESS:
                    if (m != null) {
                        m.progress(msg.arg1, msg.arg2);
                    }
                    break;
                case PROGRESS_CALLBACK_ERROR:
                    if (m != null) {
                        m.error((MultiThreadTaskRef) msg.obj);
                    }
                    break;
                case PROGRESS_CALLBACK_IO_ERROR:
                    if (m != null) {
                        m.progress.ioError(String.valueOf(msg.obj));
                    }
                    break;
                default:

                    break;
            }
        }
    }

    public static void main(String[] args) {

        MultiThreadDownTool mtdt = new MultiThreadDownTool();
        mtdt.start("http://www.bjoil.com/bjoil/shouji/beijing_petroleum_membership.apk",
                "/Users/ldm/Downloads",
                "zwjs.dmg",
                new ProgressCallback() {

                    @Override
                    public void success(String path) {
                        System.out.println(path);
                    }

                    @Override
                    public void stagesToComplete(int tCount, int index) {
                        System.out.println(tCount + " - " + index);
                    }

                    @Override
                    public void progress(int index, long count, long current) {
                        System.out.println("线程编号【" + index + "】, " + count + "-" + current);
                    }

                    @Override
                    public void error(String error) {
                        System.out.println(error);
                    }
                },
                5
        );
    }

    /**
     * 这个线程包含了分析任务状态及创建新任务的流程
     */
    private class TaskAnalyseThread extends Thread {
        private final String url;
        private final String fileName;
        private final ProgressCallback progress;
        private final int threadCount;

        public TaskAnalyseThread(String url, String fileName, ProgressCallback progress, int threadCount) {
            this.url = url;
            this.fileName = fileName;
            this.progress = progress;
            this.threadCount = threadCount;
        }

        @Override
        public void run() {
        	//降低这个线程的优先级
        	android.os.Process.setThreadPriority(10);
            try {
                //每次都将初始化一个FileDownloadRecord
                fdr = checkFileDownloadComplete(filePath);
                //文件name相同时表示同一个文件
                if (fdr != null && filePath.equals(fdr.getFilePath()) && fdr.getCurrent() <= fdr.getThreadTotal()) {
                    if (fdr.isComplete() && fdr.getMultiThreadTaskRef() != null && fdr.getCurrent() == fdr.getMultiThreadTaskRef().size()) {
                        //下载完成，执行播放操作
                        if (progress != null) {
                            progress.success(progress.asynchronous(filePath));
                        }
                        stopTask();
                    } else {
                        //继续上一次的FileDownloadRecord信息下载
                        startForFdr(url);
                    }
                } else {
                    if (fdr != null) {
                        //如果存在旧版本文件先删除
                        FileTool.delete(fdr.getFilePath());
                        FileTool.delete(fdr.getFilePath() + DOWNLOAD_RECORD_SUFFIX);
                        fdr = null;
                    }
                    fdr = new FileDownloadRecord();
                    fdr.setUrl(url);
                    fdr.setFileName(fileName);
                    fdr.setFilePath(filePath);//用来区分任务使用
                    HttpClient c = new DefaultHttpClient();
                    HttpGet get = new HttpGet(url);
                    HttpResponse response = c.execute(get);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        /** 获取文件大小 */
                        HttpEntity entity = response.getEntity();
                        fileSize = entity.getContentLength();
                        boolean b = false;
                        if (fileSize > 0) {//当无法获取size时忽略本次
                            //本地创建一个一样大小的文件
                            RandomAccessFile raf = new RandomAccessFile(filePath, MODE);
                            raf.setLength(fileSize);
                            raf.close();
                            b = true;
                        } else {
                            File f = new File(filePath);
                            if (f.exists() || f.createNewFile()) {
                                b = true;
                            }
                        }
                        if (!b) {//文件创建失败，提示用户下载失败
                            if (progress != null) {
                                progress.error("文件创建失败，请查看SD卡状态！");
                            }
                            stopTask();
                            return;
                        }
                        //记录任务文件大小
                        fdr.setFileSize(fileSize);
                        if (threadCount == 1 || fileSize <= MIN_LENGTH) {
                            /***********************************
                             *          执行单线程下载           *
                             ***********************************/
                            fdr.setThreadTotal(1);//记录线程总数，用来计算文件下载是否完成
                            downloads = new Download[1];
                            Download download = new Download(entity.getContent(), 0, 0);
                            downloads[0] = download;

                            //记录线程信息
                            List<MultiThreadTaskRef> records = new ArrayList<MultiThreadTaskRef>();
                            records.add(new MultiThreadTaskRef(url, 0, 0, fileSize));
                            fdr.setMultiThreadTaskRef(records);

                            //下载
                            if (!download.down()) {
                                //下载失败
                                securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_ERROR, new MultiThreadTaskRef(records.get(0))));
                            } else {
                                //下载成功
                                securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_STAGES_TO_COMPLETE, 0, 0, filePath));
                            }
                        } else {
                            /***********************************
                             *          执行多线程下载           *
                             ***********************************/
                            try {
                                //断开连接
                                c.getConnectionManager().shutdown();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            fdr.setThreadTotal(threadCount);//记录线程总数，用来计算文件下载是否完成
                            downloads = new Download[threadCount];
                            /** 每个线程负责的大小 */
                            long singlesSize = fileSize / threadCount;
                            //开始创建线程
                            createThread(singlesSize, url);
                        }
                    } else {
                        //下载失败
                        securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_ERROR, null));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                securityHandler.sendMessage(securityHandler.obtainMessage(PROGRESS_CALLBACK_IO_ERROR, e.getMessage()));
            }
        }
    }
}