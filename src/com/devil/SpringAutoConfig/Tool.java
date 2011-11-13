package com.devil.SpringAutoConfig;

import java.io.File;
import java.io.IOException;

public class Tool {
	protected static String getFullPath(String rawPath){
		String result=null;
		try {
			result = new File(System.getProperty("user.dir")+File.separator+rawPath).getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}