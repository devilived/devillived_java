package com.devil.autoRedo;

/**
 * 抛出该异常将继续执行任务
 * @author devilived
 *
 */
public class NeedRedoException extends Exception {
	private static final long serialVersionUID = 1L;

	public NeedRedoException(){}
	public NeedRedoException(Throwable t){
		super(t);
	}
}
