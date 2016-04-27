package com.ldm.basic.utils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by ldm on 13-6-4.
 * 提供TaskThread功能，单线程多任务模式，详细使用见代码中的各个方法注释
 */
public class TaskThreadService {

    private TaskThread taskThread;
    private boolean startTask;//true启用
    private boolean execution;

    /**
     * 创建一个带有TaskThread功能的service
     *
     * @param startTask true启动  false不启动
     */
    public TaskThreadService(boolean startTask) {
        if (this.startTask = startTask) {
            startTask();
        }
        init();
    }

    /**
     * 当创建TaskThreadService时没有开始任务时可以用次方法打开任务服务
     */
    public void startTask() {
        this.startTask = true;
        taskThread = new TaskThread();
        taskThread.start();
    }

    /**
     * 初始化
     */
    private void init() {
    }

    /**
     * 该方法需要使用BasicService(true)创建时才可使用否则会引发异常
     *
     * @param task 该任务为空时系统将自动清除后面的所任务，但线程不会停止
     */
    public void addTask(Task task) {
        if (taskThread == null) {
            Log.e("您没有启用任务功能，不能调用该方法！");
            return;
        }

        synchronized (taskThread.taskQueue) {
            taskThread.taskQueue.add(task);//将任务添加到队列最后面
            taskThread.taskQueue.notify();//解锁
        }
    }

    /**
     * 获取当前任务数量
     *
     * @return i
     */
    public int getTaskCount() {
        return taskThread.taskQueue.size() + (execution ? 1 : 0);
    }

    /**
     * 停止任务线程
     * 该方法非强制停止，系统会等待当前处理的任务完成时停止后面的所有任务
     */
    public void stopTask() {
        this.startTask = false;
        if (taskThread != null) {
            taskThread.stopTask();
        }
    }

    /**
     * 任务功能是否开启
     *
     * @return true任务模式已经开启
     */
    public boolean isStartTask() {
        return this.startTask;
    }



    /**
     * 多任务线程
     * 如果该线程中的任务队列中遇到StopTask任务，默认视为停止执行的信号, 系统会自动清除后面的任务，即使队列中有没执行的任务仍然执行清除操作
     * 清空所有任务后该线程并不停止仍可继续使用，直到用户用调用了stopTask方法停止该线程
     */
    public class TaskThread extends Thread {

        //控制线程是否继续运行的条件
        private boolean isRun;

        /**
         * 用户任务队列,如果队列中遇到为null的任务，默认视为停止执行的信号
         * 如果该队列本身等于null，将引发不可预料的错误
         */
        public final Queue<Task> taskQueue;

        public TaskThread() {
            isRun = true;
            taskQueue = new LinkedList<>();
        }

        @Override
        public void run() {
        	android.os.Process.setThreadPriority( android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while (isRun) {
            	execution = true;
                /***
                 * 如果没有任务则等待用户添加任务
                 * 如果有任务则进行处理
                 */
                if (taskQueue.size() == 0) {
                    synchronized (taskQueue) {
                        try {
                            taskQueue.wait();//加锁 进入无限等待状态
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Task task = getTask();
                    if (task == null) continue;
                    if (task.isStop) {//此处视为用户停止任务队列操作
                        taskQueue.clear();//清空任务队列，等待用户重新加入任务
                        isRun = false;//停止线程继续向下执行任务
                    } else {
                        //进行任务操作
                        task.taskStart(task.param);
                    }
                    if (task.sleepTime > 0) {
                    	try {
                    		sleep(task.sleepTime);
                    	} catch (InterruptedException e) {
                    		e.printStackTrace();
                    	}
					}
                }
                execution = false;
            }
        }

        /**
         * 根据队列的规则进行弹出任务操作
         *
         * @return Task
         */
        private Task getTask() {
            synchronized (taskQueue) {
                return taskQueue.isEmpty() ? null : taskQueue.poll();//将队列头元素返回并在列表中删除
            }
        }

        /**
         * 此方法非强制停止操作，
         * 而是利用了isRun间接式的停止线程及释放处于wait状态的队列
         */
        public void stopTask() {
            if (taskQueue.size() > 0) {
                taskQueue.clear();
            }
            try {
                //如果该线程处于wait状态，利用taskQueue遇到空值的机制将其清理
                synchronized (taskQueue) {
                    taskQueue.add(new StopTask());
                    taskQueue.notify();//解锁
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用户任务接口
     * 1、用户需要在taskStart回调中处理可能会发生的异常， 如果用户抛出异常将会导致此次创建的TaskThread无法继续使用
     * 2、如果用户需要停止taskThread时，需要调用该service的stopTask方法，但stopTask无法停止当前处于运行的任务，
     * 所以用户需要根据当前任务类型在taskStart中提前执行return;操作，以配合stopTask方法停止线程
     * 3、taskStart中不支持做UI更新操作，需要用户配合handler处理UI更新问题
     * 4、用户可以通过在构造Task时传入一个或多个Object参数， 该参数用户可以在taskStart中拿到
     */
    public static abstract class Task {
        protected boolean isStop;
        public int sleepTime;//本次任务执行后间隔时间
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
    	void onTaskNumberEmptyListener();
	}

}