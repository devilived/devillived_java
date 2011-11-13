package com.devil.utils;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

public final class XmlUtil {
	public static Document getXmlDocument(String path){
		Document doc=null;
		File file=new File(path);
		if(file.exists()){
			SAXReader reader = new SAXReader();
			try {
				doc = reader.read(file);
			} catch (Exception e) {
				System.err.println("xml文件路径出错！");
			}
		}
		return doc;
	}

}
