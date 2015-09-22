package com.devil.autoRedo;

/**
 * 抛出该异常将中断程序运行
 * @author devilived
 */
public class CantRedoException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public CantRedoException(String detail){
		super(detail);
	}
	public CantRedoException(Throwable t){
		super(t);
	}
}
