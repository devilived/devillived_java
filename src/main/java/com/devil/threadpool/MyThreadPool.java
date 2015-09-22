package com.devil.threadpool;

import java.util.LinkedList;

public class MyThreadPool extends ThreadGroup {
	private boolean isClosed = false; // 线程池是否关闭标志
	private LinkedList<Runnable> workQueue=new LinkedList<Runnable>(); // 工作线程队列

	public MyThreadPool(int poolSize) {  //poolSize 表示线程池中的保持的线程数量  
        super("test thread pool");      //指定ThreadGroup的名称  
        setDaemon(true);               //继承到的方法，设置是否守护线程池  
        for(int i = 0; i < poolSize; i++) {  
            new WorkThread(i).start();   //创建并启动线程池线程       
        }
    }
	
	/** 向工作队列中加入一个新任务,由工作线程去执行该任务 */
	public synchronized void execute(Runnable task) {
		if (isClosed) {
			throw new IllegalStateException("线程池已经关闭");
		}
		if (task != null) {
			workQueue.add(task);// 向队列中加入一个任务
			notify(); // 唤醒一个正在getTask()方法中待任务的工作线程
		}
	}

	/** 从工作队列中取出一个任务,工作线程会调用此方法 */
	private synchronized Runnable getTask(int threadid){
		while (workQueue.size() == 0) {
			if (isClosed){
				return null;
			}
			try {
				// 如果工作队列中没有任务,就等待任务,当前线程等待，直到其他线程调用该对象的notify()方法
				System.out.println("工作线程" + threadid + "等待任务...");
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		System.out.println("工作线程" + threadid + "开始执行任务...");
		return workQueue.removeFirst(); // 反回队列中第一个元素,并从队列中删除
	}

	/** 关闭线程池 */
	public synchronized void closePool() {
		if (!isClosed) {
			waitFinish(); // 等待工作线程执行完毕
			isClosed = true;
			workQueue.clear(); // 清空工作队列
			interrupt(); // 中断线程池中的所有的工作线程,此方法继承自ThreadGroup类
		}
	}

	/** 等待工作线程把所有任务执行完毕 */
	public void waitFinish() {
		synchronized (this) {
			isClosed = true;
			notifyAll(); // 唤醒所有还在getTask()方法中等待任务的工作线程
		}
		Thread[] threads = new Thread[activeCount()];
		System.out.println("threads" + activeCount());
		// activeCount() 返回该线程组中活动线程的估计值。
		int count = enumerate(threads); // enumerate()方法继承自ThreadGroup类，根据活动线程的估计值获得线程组中当前所有活动的工作线程
		for (int i = 0; i < count; i++) { // 等待所有工作线程结束
			try {
				// 表示当前线程停止执行直到t线程被中断或者正常结束后才正常执行当前线程的其余代码
				threads[i].join(); // 等待工作线程结束,指在一线程里面调用另一线程join方法时，表示将本线程阻塞直至另一线程终止时再执行

			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 内部类,工作线程,负责从工作队列中取出任务,并执行
	 * 
	 */
	private class WorkThread extends Thread {
		private int id;

		public WorkThread(int id) {
			// 父类构造方法,将线程加入到当前ThreadPool线程组中
			super(MyThreadPool.this, id + "");
			this.id = id;
		}

		public void run() {
			while (!isInterrupted()) { // isInterrupted()方法继承自Thread类，判断线程是否被中断
					// 取出任务，初始化线程池线程的时候，
					//因为没有工作线程，所以都进入Wait状态
					Runnable task = getTask(id); 
					
					// 如果getTask()返回null或者线程执行getTask()时被中断，则结束此线程
					if (task != null) {
						System.out.println("ThreadPool's Thread name is :"+ WorkThread.class.getName() + id);
						task.run(); // 运行任务
					}
			}
		}
	}
}