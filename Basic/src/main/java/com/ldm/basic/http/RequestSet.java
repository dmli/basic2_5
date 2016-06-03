package com.ldm.basic.http;


import java.util.List;

/**
 * ldm 2012-4-16  
 * 下午12:50:55 RequestSet.java 批量网络请求集合
 */
public class RequestSet {

    RequestTaskInfo taskInfo;

	List<HttpRequestClient> _cs;

	public RequestSet(List<HttpRequestClient> child) {
		this._cs = child;
	}

    void setTaskInfo(RequestTaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    /**
	 * 当该任务开始处理时调用，该方法优先于start (主线程中被调用)
	 */
	public void enter() {
	}

	/**
	 * 当该任务处理完成后被调用(主线程中被调用)
	 */
	public void end() {
	}

	/**
	 * 网络异常时被触发
	 */
	public void ioError() {
	}

	/**
	 * 获取用户的所有请求
	 *
	 * @return HttpRequestChild[]
	 */
	public List<HttpRequestClient> getChild() {
		return _cs;
	}
}
