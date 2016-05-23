package com.zzg.logservice.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;

//import com.lidroid.xutils.HttpUtils;
//import com.lidroid.xutils.exception.HttpException;
//import com.lidroid.xutils.http.RequestParams;
//import com.lidroid.xutils.http.ResponseInfo;
//import com.lidroid.xutils.http.callback.RequestCallBack;
//import com.lidroid.xutils.http.client.HttpRequest;
import com.zzg.logservice.utils.ToolsFile;

/**
 * @author Abelzzg
 * @version V1.0
 * @Description: 往服务器发送log日志
 */
public class CustomHttpUtils {

	// private PreferencesCookieStore preferencesCookieStore;
	private CustomHttpUtils(){
		
	}
	static CustomHttpUtils instance;
	
	public static CustomHttpUtils getInstance(){
		if (instance==null) {
			instance = new CustomHttpUtils();
		}
		return instance;
	}
	/**
	 * 是否注册成功
	 */
	static Boolean isRegisted = false;

	private static final String registUrl = "";

	public static boolean registLog(String appkey) {
//		RequestParams requestParams = new RequestParams();
//		requestParams.addBodyParameter("app_key", appkey);
//		HttpUtils httpUtils = new HttpUtils();
//		httpUtils.send(HttpRequest.HttpMethod.POST, registUrl, requestParams,
//				new RequestCallBack<String>() {
//					@Override
//					public void onStart() {
//					}
//
//					@Override
//					public void onLoading(long total, long current,
//							boolean isUploading) {
//						if (isUploading) {
//						} else {
//						}
//					}
//
//					@Override
//					public void onSuccess(ResponseInfo<String> responseInfo) {
//						String rs = responseInfo.result;
//						isRegisted = rs.equals("success");
//					}
//
//					@Override
//					public void onFailure(HttpException error, String msg) {
//
//					}
//				});
		return true;
	}

	// /**
	// * 往服务器上传文件
	// *
	// * @param filePath 发送的文件路径
	// * @param serviceUrl 接受的服务器地址
	// */
	// public void upload(String filePath, String serviceUrl) {
	// if (!Tools.isFastMobileNetwork()) {
	// return;
	// }
	// File file = new File(filePath);
	// if (ToolsFile.isFileExit(filePath)) {
	// upload(file, serviceUrl);
	// }
	// }

	// /**
	// * 上传文件
	// *
	// * @param file 上传的文件
	// * @param serviceUrl 上传的服务器地址
	// */
	// private void upload(File file, String serviceUrl) {
	//
	// // 设置请求参数的编码
	// //RequestParams params = new RequestParams("GBK");
	// RequestParams params = new RequestParams(); // 默认编码UTF-8
	// // 添加文件
	// params.addBodyParameter("android_log", file);
	// //params.addBodyParameter("testfile", new File("/sdcard/test2.zip")); //
	// 继续添加文件
	//
	// HttpUtils http = new HttpUtils();
	// // 设置返回文本的编码， 默认编码UTF-8
	// //http.configResponseTextCharset("GBK");
	// // 自动管理 cookie
	// // http.configCookieStore(preferencesCookieStore);
	// http.send(HttpRequest.HttpMethod.POST, serviceUrl
	// ,
	// params,
	// new RequestCallBack<String>() {
	// @Override
	// public void onStart() {
	// }
	//
	// @Override
	// public void onLoading(long total, long current, boolean isUploading) {
	// if (isUploading) {
	// } else {
	// }
	// }
	//
	// @Override
	// public void onSuccess(ResponseInfo<String> responseInfo) {
	// System.out.println("reply:" + responseInfo.result);
	// }
	//
	// @Override
	// public void onFailure(HttpException error, String msg) {
	// }
	// });
	// }
	int time = 1;

	/**
	 * 上传文件
	 * 
	 * @param json
	 *            上传的json字符串
	 * @param serviceUrl
	 *            上传的服务器地址
	 */
	public synchronized void uploadString(final String json,
			final String serviceUrl) {
		if (time == 1) {
			time -= 1;
			new Thread() {
				public void run() {
					Log.i("com.yeepay.logservice","发送日志！"+serviceUrl + json);
					/* 建立HTTP Post连线 */
					HttpPost httpRequest = new HttpPost(serviceUrl);
					
					// Post运作传送变数必须用NameValuePair[]阵列储存
					// 传参数 服务端获取的方法为request.getParameter("name")
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("log", json));
					
					try {
						// 发出HTTP request
						httpRequest.setEntity(new UrlEncodedFormEntity(params,
								HTTP.UTF_8));
						// 取得HTTP response
						HttpResponse httpResponse = new DefaultHttpClient()
								.execute(httpRequest);
						// 若状态码为200 ok
						if (httpResponse.getStatusLine().getStatusCode() == 200) { // 响应码等于200,请求成功
							// 访问成功. 做相应处理.
							Log.i("com.yeepay.logservice","上传成功");
							ToolsFile.deleteFile();
							time += 1;
						}

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
					}
				};
			}.start();
		}
		// RequestParams params = new RequestParams(); // 默认编码UTF-8
		// // 添加文件
		// params.addBodyParameter("log", json);
		// System.out.println("time = " + time);
		// HttpUtils http = new HttpUtils();
		// http.send(HttpRequest.HttpMethod.POST, serviceUrl, params,
		// new RequestCallBack<String>() {
		// @Override
		// public void onStart() {
		// }
		//
		// @Override
		// public void onLoading(long total, long current,
		// boolean isUploading) {
		// }
		//
		// @Override
		// public void onSuccess(ResponseInfo<String> responseInfo) {
		// System.out
		// .println("发送成功 time=" + time + " reply:"
		// + responseInfo.statusCode
		// + responseInfo.result);
		// List<File> files = ToolsFile
		// .searchFiles(AppConstant.logDir);// 查找文件
		// for (int i = 0; i < files.size(); i++) {
		// if (!files.get(i).getName()
		// .equals(LogService.logName)) {// 过滤files
		// System.out.println("删除"
		// + files.get(i).getName());
		// ToolsFile.deleteFile(files.get(i));// 删掉文件
		// }
		// }
		// }
		//
		// @Override
		// public void onFailure(HttpException error, String msg) {
		// System.out.println("发送失败reply:" + msg);
		// }
		// });
		// List<NameValuePair> keyvalue = new ArrayList<NameValuePair>();
		// keyvalue.add(new BasicNameValuePair("log", json));
		// HttpResponse httpResponse = null;
		// HttpPost httpPost = new HttpPost(serviceUrl);
		// try {
		// httpPost.setEntity(new UrlEncodedFormEntity(keyvalue, HTTP.UTF_8));
		// httpResponse = new DefaultHttpClient().execute(httpPost);
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (ClientProtocolException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

}
