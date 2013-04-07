package com.devil.exceptions;

public class CodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private int code = -1;
	
	public static final int CODE_NO_NET=10;

	public CodeException(int code) {
		super("code:" + code);
	}

	public CodeException(int code, String msg) {
		super("code:" + code + "->" + msg);
	}

	public CodeException(int code, Throwable e) {
		super("code:" + code, e);
	}

	public int getCode() {
		return code;
	}
}
