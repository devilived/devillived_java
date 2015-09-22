package com.devil.threadpool;

import com.devil.threadpool.MyThreadPool;

public class Main {
	public static void main(String[] args) throws InterruptedException {  
        MyThreadPool threadPool = new MyThreadPool(2); //创建一个有个3工作线程的线程池，同时都是wait状态  
        Thread.sleep(500); //休眠500毫秒,以便让线程池中的工作线程全部运行  
        //运行任务  
        for (int i = 0; i <=5 ; i++) { //创建6个任务  
         System.out.println("创建工作线程："+i);
            threadPool.execute(createTask(i));  
        }  
        threadPool.waitFinish(); //等待所有任务执行完毕  
        threadPool.closePool(); //关闭线程池  
 
    }  
 
    private static Runnable createTask(final int taskID) {  
        return new Runnable() {  
            public void run() {  
              System.out.println("Task" + taskID + "开始");  
                System.out.println("Hello world");  
              System.out.println("Task" + taskID + "结束");  
            }  
        };  
    }   
}
