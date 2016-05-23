package com.zzg.logservice.exception;

/**
 * 自定义sdcard的一些错误
 * 
 * @author Abelzzg
 * 
 */
public class SdcardException extends Exception {

	private static final long serialVersionUID = 1L;
	/** sd卡错误 */
	public static final int SDCARD_ERROR = 1;
	/** 其他错误 */
	public static final int OTHE＿ERROR = 2;
	/** sd卡已满 */
	public static final int SDCARD_FULL = 3;
	/** 错误代码 */
	private int errorCode;
	/**
	 * 获得错误代码
	 * @return 错误代码
	 */
	public int getErrorCode() {
		return errorCode;
	}

	public SdcardException() {
		super();
	}

	public SdcardException(String string) {
		super(string);
	}

	public SdcardException(String string, Throwable throwable) {
		super(string, throwable);
	}

	public SdcardException(String string, int errorCode) {
		super(string);
		this.errorCode = errorCode;
	}
}
