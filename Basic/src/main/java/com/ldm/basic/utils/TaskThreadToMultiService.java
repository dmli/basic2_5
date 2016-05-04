package com.ldm.basic.utils;

import com.ldm.basic.BasicTimerTask;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;

/**
 * Created by ldm on 13-6-4.
 * 提供TaskThread功能，多线程多任务模式，详细使用见代码中的各个方法注释
 */
public class TaskThreadToMultiService {

    private final List<TaskThread> taskThreads = new ArrayList<>();
    /**
     * 用户任务队列, 如果队列中遇到为null的任务，默认视为停止执行的信号 如果该队列本身等于null，将引发不可预料的错误
     */
    public final Queue<Task> taskQueue = new LinkedList<>();
    private int MAX_THREAD_NUMBER;
    private int MAX_RETAIN_FREE_THREAD_NUMBER = 1;// 最大的空闲线程保留数
    private Timer timer;

    /**
     * 创建一个带有TaskThread功能的service
     *
     * @param threadNumber 任务数量
     */
    public TaskThreadToMultiService(int threadNumber) {
        init(threadNumber, 3000 * 10);
    }

    /**
     * 创建一个带有TaskThread功能的service
     *
     * @param threadNumber  任务数量
     * @param keepAliveTime 线程空闲时间,超过最大空闲时间的任务将会被释放掉，不传时默认30秒
     */
    public TaskThreadToMultiService(int threadNumber, int keepAliveTime) {
        init(threadNumber, keepAliveTime);
    }

    /**
     * 初始化
     *
     * @param threadNumber  最大线程数
     * @param keepAliveTime 线程空闲时间,超过最大空闲时间的任务将会被释放掉
     */
    private void init(int threadNumber, int keepAliveTime) {
        MAX_THREAD_NUMBER = threadNumber;
        TaskThread tt = new TaskThread();
        taskThreads.add(tt);
        tt.start();
        /**
         * 启动一个无线循环的定时器，用来检测任务的空闲度
         */
        timer = new Timer();
        timer.scheduleAtFixedRate(checkThreadFreeTask, keepAliveTime, keepAliveTime);
    }

    /**
     * 设置最大的空闲线程保留数量
     *
     * @param number 最大数量（取值范围在 0 - MAX_THREAD_NUMBER 之间
     *               如果传入数量大于MAX_THREAD_NUMBER时将被截取）
     */
    public void setMaxRetainFreeThreadNumber(int number) {
        MAX_RETAIN_FREE_THREAD_NUMBER = Math.min(number, MAX_THREAD_NUMBER);
    }

    /**
     * 该方法需要使用BasicService(true)创建时才可使用否则会引发异常
     *
     * @param task 该任务为空时系统将自动清除后面的所任务，但线程不会停止
     */
    public void addTask(Task task) {
        synchronized (taskQueue) {
            if (taskQueue.size() > 0 || taskThreads.size() <= 0) {
                /**
                 * 这里表示任务堆住了, 检查线程数量是否达到了最大值，如果没有继续开启线程
                 */
                if (taskThreads.size() < MAX_THREAD_NUMBER) {
                    TaskThread t = new TaskThread();
                    taskThreads.add(t);
                    t.start();
                }
            }
            taskQueue.add(task);// 将任务添加到队列最后面
            taskQueue.notify();// 解锁
        }
    }

    /**
     * 停止任务线程 该方法非强制停止，系统会等待当前处理的任务完成时停止后面的所有任务
     */
    public void stopTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            checkThreadFreeTask = null;
        }
        try {
            // 如果该线程处于wait状态，利用taskQueue遇到空值的机制将其清理
            synchronized (taskQueue) {
                int count = taskQueue.size();
                if (count > 0) {
                    /**
                     * 经过测试，这里不能用taskQueue.clear();方法，这个方法将导致线程中的wait()方法无法解锁，
                     * 而导致线程无法被回收
                     */
                    for (int i = 0; i < count; i++) {
                        taskQueue.poll();
                    }
                }
                count = taskThreads.size();
                // 根据剩余线程的数量添加StopTask()任务，这样可以保证销毁所有的线程
                for (int i = 0; i < count; i++) {
                    taskQueue.add(new StopTask());
                    taskQueue.notify();// 解锁
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 多任务线程 如果该线程中的任务队列中遇到StopTask任务，默认视为停止执行的信号,
     * 系统会自动清除后面的任务，即使队列中有没执行的任务仍然执行清除操作
     * 清空所有任务后该线程并不停止仍可继续使用，直到用户用调用了stopTask方法停止该线程
     */
    public class TaskThread extends Thread {

        private boolean isRun;
        public boolean isWait;// 是否处于等待中

        public TaskThread() {
            isRun = true;
            isWait = false;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while (isRun) {
                /***
                 * 如果没有任务则等待用户添加任务 如果有任务则进行处理
                 */
                if (taskQueue.size() == 0) {
                    synchronized (taskQueue) {
                        if (!isRun) {
                            break;
                        }
                        try {
                            isWait = true;
                            taskQueue.wait();// 加锁 进入无限等待状态
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Task task = getTask();
                    if (task == null) {
                        continue;
                    }
                    isRun = !task.isStop;
                    if (isRun) {
                        isWait = false;
                        // 进行任务操作
                        task.taskStart(task.param);
                        if (task.sleepTime > 0) {
                            try {
                                sleep(task.sleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            // 这里表示线程被释放掉了
            if (!isRun) {
                synchronized (taskThreads) {
                    // 在任务队列中删除
                    taskThreads.remove(this);
                }
            }
        }

        /**
         * 根据队列的规则进行弹出任务操作
         *
         * @return Task
         */
        private Task getTask() {
            synchronized (taskQueue) {
                return taskQueue.isEmpty() ? null : taskQueue.poll();// 将队列头元素返回并在列表中删除
            }
        }

    }

    /**
     * 用户任务接口 1、用户需要在taskStart回调中处理可能会发生的异常， 如果用户抛出异常将会导致此次创建的TaskThread无法继续使用
     * 2、如果用户需要停止taskThread时，需要调用该service的stopTask方法，但stopTask无法停止当前处于运行的任务，
     * 所以用户需要根据当前任务类型在taskStart中提前执行return;操作，以配合stopTask方法停止线程
     * 3、taskStart中不支持做UI更新操作，需要用户配合handler处理UI更新问题
     * 4、用户可以通过在构造Task时传入一个或多个Object参数， 该参数用户可以在taskStart中拿到
     */
    public static abstract class Task {
        protected boolean isStop;
        private int sleepTime;// 本次任务执行后间隔时间
        public Object[] param;

        protected Task(Object... obj) {
            sleepTime = 0;
            param = obj;
            isStop = false;
        }

        public Task setSleepTime(int time) {
            this.sleepTime = time;
            return this;
        }

        public abstract void taskStart(Object... obj);
    }

    /**
     * 任务停止使用
     */
    public static class StopTask extends Task {

        protected StopTask(Object... obj) {
            super(obj);
            isStop = true;
        }

        @Override
        public void taskStart(Object... obj) {

        }
    }

    public interface OnTaskNumberEmptyListener {
        public void onTaskNumberEmptyListener();
    }

    /**
     * 检查线程空限度的定时器任务
     */
    private BasicTimerTask checkThreadFreeTask = new BasicTimerTask("check_thread_free_task") {
        @Override
        public void run() {
            /**
             * 这里先检测任务队列是否有任务，仅在没有任务的时候才能进行任务释放
             */
            if (taskQueue.size() <= 0) {
                synchronized (taskQueue) {

                    final int maxThread = taskThreads.size();
                    /**
                     * 当线程数大于MAX_RETAIN_FREE_THREAD_NUMBER时，执行释放操作
                     */
                    if (maxThread > MAX_RETAIN_FREE_THREAD_NUMBER) {
                        int freeThreadNumber = 0;// 线程的空闲数
                        for (int i = 0; i < maxThread; i++) {
                            if (taskThreads.get(i).isWait) {
                                freeThreadNumber++;
                            }
                        }
                        /**
                         * 利用StopTask任务释放处于空闲中的线程
                         */
                        final int count = Math.min(maxThread - MAX_RETAIN_FREE_THREAD_NUMBER, freeThreadNumber);
                        if (count > 0) {
                            for (int i = 0; i < count; i++) {
                                taskQueue.add(new StopTask());
                            }
                            taskQueue.notify();// 解锁
                        }
                    }
                }
            }
        }
    };

    public static void main(String[] args) {
        TaskThreadToMultiService t = new TaskThreadToMultiService(50);
        System.out.println("任务启动");
        for (int i = 0; i < 300; i++) {
            Task task = new Task(i) {
                @Override
                public void taskStart(Object... obj) {
                    System.out.println("任务号 " + obj[0]);
                }
            }.setSleepTime(30);
            t.addTask(task);

        }
        t.stopTask();
    }
}
