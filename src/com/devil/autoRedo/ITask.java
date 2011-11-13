package com.devil.autoRedo;
/**
 * 任务接口
 * @author devilived
 *
 */
public interface ITask {
	public String getTaskName();
	public Object runTask(Object param)throws NeedRedoException,CantRedoException;
}
