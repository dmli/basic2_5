package com.ldm.basic.conn;

import org.apache.http.HttpEntity;

import com.ldm.basic.bean.BasicInternetRetBean;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/**
 * 单任务线程
 * @author ldm
 */
public class RequestSingleThread extends Thread implements OnClickListener{
	private Context context;
	private MultiSecurityHandler<Context> handler;

	private String url;
	private HttpEntity[] content;
	private String mode;

	private int index;
	private RequestRetCallBack callBack;

	private boolean isStop;

	/**
	 * 创建一个单任务的线程
	 *
	 * @param context 上下文
	 * @param handler 更新UI用
	 * @param url 地址
	 * @param content 内容
	 * @param mode 类型 - HTTP_MODE_POST --- HTTP_MODE_GET
	 * @param index 伪随机数
	 * @param callBack 回调接口
	 */
	public RequestSingleThread(Context context, MultiSecurityHandler<Context> handler, String url, HttpEntity[] content, String mode, int index, RequestRetCallBack callBack) {
		this.context = context;
		this.handler = handler;
		this.url = url;
		this.content = content;
		this.mode = mode;
		this.index = index;
		this.callBack = callBack;
		this.isStop = false;
	}

	@Override
	public void run() {
		if (isStop || handler == null) {
			return;
		}

		handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_CHILD_ENTER, callBack));// 触发任务接口

		BasicInternetRetBean bib;
		if (mode.equals(InternetEntity.HTTP_MODE_DELETE)) {
			bib = new BasicDeleteHelper().http(url);
		} else if (mode.equals(InternetEntity.HTTP_MODE_GET)) {
			bib = new BasicGetHelper().http(url, callBack.requestType);
		} else if (mode.equals(InternetEntity.HTTP_MODE_PUT)) {
			bib = new BasicPutHelper().http(url, callBack.requestType, content);
		} else {
			if (url == null) {
				bib = new BasicPostHelper().http(context, callBack.requestType, content);
			} else {
				bib = new BasicPostHelper().http(url, callBack.requestType, content);
			}
		}
		onResult(bib);
	}

	private void onResult(BasicInternetRetBean bib) {
		if (bib != null) {// 此处数据返回
			if (bib.getCode() == 0) {
				if (bib.getSuccess() != null && !"".equals(bib.getSuccess()) && !"[]".equals(bib.getSuccess())) {
					callBack.data = callBack.asynchronous(bib.getSuccess());
					handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_SUCCESS, callBack));// 数据处理成功
				} else {
					handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_RET_NULL, callBack));// 返回数据为空，默认使用retNull函数
				}
			} else if (bib.getCode() == 1) {
				callBack.data = bib.getError();
				callBack.code = bib.getResponseCode();
				handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_ERROR, callBack));
			} else if (bib.getCode() == 2) {
				handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_IS_NETWORK_STATE, callBack));
				handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_IO_ERROR, callBack));
			}
		} else {
			callBack.data = "数据解析失败，请稍候再试！";
			callBack.code = -1;
			handler.sendMessage(handler.obtainMessage(InternetEntity.RESULT_ERROR, callBack));
		}
		handler.sendMessage(handler.obtainMessage(index, callBack));// 接口全部执行完后调用
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		isStop = true;
		dialog.dismiss();
		index = 0;// 停止handler继续接收消息
	}
}
