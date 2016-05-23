package com.zzg.logservice.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.zzg.logservice.config.AppConstant;
import com.zzg.logservice.service.LogService;
import com.zzg.logservice.utils.Tools;

/**
 * 自定义全局异常类
 * 
 * @author Abelzzg
 * 
 */
public class CustomException implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CustomException INSTANCE = new CustomException();
	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
//	private Map<String, String> infos = new HashMap<String, String>();

	// 用于格式化日期,作为日志文件名的一部分
//	private DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

	/** 保证只有一个CrashHandler实例 */
	private CustomException() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CustomException getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.i("com.yeepay.logservice", "有无法处理的异常被抓获");
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Log.e(TAG, "error : ", e);
			}
			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		ex.printStackTrace();
		if (ex == null) {
			return false;
		}
		// 使用Toast来显示异常信息
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG)
						.show();
				Looper.loop();
			}
		}.start();
		// 保存日志文件
		saveCrashInfo(ex);
		return true;
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo(Throwable ex) {
		writeCrashLog(ex);
		return null;
	}

	private void writeCrashLog(Throwable ex) {
		// 对ex对象进行处理
		// 只要前三行
		String sOut = "";
		StackTraceElement[] trace = ex.getStackTrace();
		for (int i = 0; i < 3; i++) {
			sOut += "|" + trace[i];
		}
		try {
			String time = Tools.getCurrentTime4Json();
			String error_code = AppConstant.CRASH_CODE;
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("time", time);
			jsonObject.addProperty("reason", ex.getClass().getSimpleName());
			jsonObject.addProperty("error_code", error_code);
			jsonObject.addProperty("stack_trace", sOut);
			LogService.saveErrorLog(jsonObject);
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}
	}
}
