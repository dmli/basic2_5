package com.ldm.basic.conn;

import com.ldm.basic.bean.BasicInternetRetBean;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/**
 * 多任务集合使用的线程
 * 
 * @author ldm
 */
public class RequestMultiThread extends Thread implements OnClickListener {
	private Context context;
	private RequestSet requestSet;
	private MultiSecurityHandler<Context> handler;
	private int index;

	private boolean isStop;

	/**
	 * 创建一个多任务使用的Thread
	 *
	 * @param context 上下文
	 * @param requestSet 请求集合
	 * @param handler MultiSecurityHandler 更新UI
	 * @param index 伪随机数
	 */
	public RequestMultiThread(Context context, RequestSet requestSet, MultiSecurityHandler<Context> handler, int index) {
		this.context = context;
		this.requestSet = requestSet;
		this.handler = handler;
		this.index = index;
		this.isStop = false;
	}

	@Override
	public void run() {
		if (isStop || handler == null || requestSet.getChild() == null)
			return;

		for (int i = 0; i < requestSet.getChild().size(); i++) {
			if (isStop)
				return;// 停止剩下的所有任务

			HttpPostChild httpPostChild = requestSet.getChild().get(i);
			handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_CHILD_ENTER, i, 0, httpPostChild));
			BasicInternetRetBean bib;
			if (httpPostChild._url == null) {
				bib = httpPostChild.start(context);
			} else {
				bib = httpPostChild.start(httpPostChild._url);
			}
			if (bib != null) {
				if (bib.getCode() == 0) {
					if (bib.getSuccess() != null && !"".equals(bib.getSuccess()) && !"[]".equals(bib.getSuccess())) {
						httpPostChild._c.data = httpPostChild._c.asynchronous(bib.getSuccess());
						handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_SUCCESS, httpPostChild._c));// 数据处理成功
					} else {
						handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_RET_NULL, httpPostChild._c));// 返回数据为空，默认使用retNull函数
					}
				} else if (bib.getCode() == 1) {
					httpPostChild._c.data = bib.getError();
					httpPostChild._c.code = bib.getResponseCode();
					handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_ERROR, httpPostChild._c));
				} else if (bib.getCode() == 2) {
					handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_IS_NETWORK_STATE, httpPostChild._c));
					handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_IO_ERROR, httpPostChild._c));
				}
			} else {
				httpPostChild._c.data = "数据解析失败，请稍候再试！";
				httpPostChild._c.code = -1;
				handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_ERROR, i, 0, httpPostChild._c));
			}
			handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_CHILD_EXIT, httpPostChild));
		}
		// 接口全部执行完成后该方法被执行，为带有对话框的请求关闭对话框
		handler.sendMessage(handler.obtainMessage(index, 2, 0, requestSet));
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		isStop = true;
		dialog.dismiss();
		index = 0;// 停止handler继续接收消息
	}
}
