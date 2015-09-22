package com.devil.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * build string with delimer, such as array [1,3,5,7,9] to string "1,3,5,7,9"
 */
public final class BuildStringWithDelimer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> list=new ArrayList<String>();
		list.add("1");
		System.out.println(build(list,null));
		
		list.add("2");
		
		list.add("3");
		
		System.out.println(build(list,null));
		
		

	}
	public static <T> String build(Collection<T> src, String delimer){
		if(src==null || src.size()==0){
			return "";
		}
		
		if(delimer==null){
			delimer=",";
		}
		
		Iterator<T> it=src.iterator();
		String rst=it.next().toString();
		
		while(it.hasNext()){
			rst+=delimer+it.next().toString();
		}
		
		return rst;
	}

}
