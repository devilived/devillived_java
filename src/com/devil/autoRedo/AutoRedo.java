package com.devil.autoRedo;

/**
 * 本类的作用是重复执行一定次数的任务。
 * 应用场景：我们访问网络时由于某个时刻可能网络繁忙，无法及时相应，这时再次访问可能就可以了。
 * 工作原理：通过实现ITask接口（一般是通过回调实现），抛出需要重复做的异常和不能重复做的异常来控制任务执行逻辑。
 * @param ITask:任务
 * @param delay:每两次任务间相隔delay毫秒
 * @param times:最多执行times次，超过后将抛出不能重复做的异常，中断程序运行，防止死循环 *
 */
public class AutoRedo {
	/**
	 * 如果任务需要参数，可以用这个构造函数
	 * @param taskParam
	 */
	public static Object run(ITask task,Object taskParam, int delay,int times) throws CantRedoException{
		String taskName=task.getTaskName();
		if(taskName==null){
			taskName="无名任务";
		}

		Object rtnVal=null;
		int i=1;
		for(;i<times+1;i++){
			try {
				rtnVal=task.runTask(taskParam);
				break;//如果某一次正常运行，那么立刻返回
			} catch (NeedRedoException e) {
				//不用理会，直接返回即可
				try {
					Thread.sleep(delay);
					System.out.println("第"+i+"运行任务 ["+taskName+"] 失败,");
				} catch (InterruptedException e1) {
					throw new CantRedoException(e1);
				}
			}catch(CantRedoException e){
				throw e;
			}catch(Throwable t){
				throw new CantRedoException(t);
			}
		}
		if(i==times+1){
			throw new CantRedoException("任务 ["+taskName+"] 已经运行"+times+"次,仍然失败，退出该任务");
		}
		
		return rtnVal;
	}
}
