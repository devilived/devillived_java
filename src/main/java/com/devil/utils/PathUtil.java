package com.devil.utils;

public class PathUtil {
	public static void main(String[] args) {
		System.out.println("userdir:"+getUserDir());
		System.out.println("userhome:"+getUserHome());
	}
	//用户执行java命令时所在的当前目录
	public static String getUserDir(){
		//return new File("").getAbsolutePath();
		//return new File(".").getAbsolutePath();
		return System.getProperty("user.dir");
	}
	public static String getUserHome(){
		return System.getProperty("user.home");
	}
}
