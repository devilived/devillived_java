package com.devil.autoRedo;

public class Main {
	public static void main(String[] args) {
		final boolean isException=true;
		
		ITask task=new ITask() {
				@Override
				public Object runTask(Object param) throws NeedRedoException, CantRedoException {
					if(isException){
						throw new NeedRedoException();
					}
					System.out.println("I am running task");
					return "runResult";
				}
				
				@Override
				public String getTaskName() {
					return "test Task";
				}
			};
		
		try {
			int n=0;
			String s = (String)AutoRedo.run(task, n, 500, 5);
			System.out.println("运行结果为："+s);
		} catch (CantRedoException e) {
			e.printStackTrace();
		}
		
	}
}
