package com.devil.SpringAutoConfig;

import org.dom4j.Document;

import com.devil.utils.XmlJDomUtil;

public class Main {
	public static void main(String args[]) {
		final String autoDaoPkg="com.litb.v3.center.database.auto.dao";
		final String autoServicePkg="com.litb.v3.center.service.impl.order";
		final String autoActionPkg="com.litb.v3.center.webaction.order";
		//以下使用默认的builder，默认实现dao,service和action的build，可以使用默认的构造函数，设定自定义builder
		SpringAutomaticConfig config=new SpringAutomaticConfig(autoDaoPkg,autoServicePkg,autoActionPkg);
		
		String tplPath = System.getProperty("user.dir") + "/src/main/resources/spring-config/app-ctx-automatic.xml.template";
		String destPath = "D:/xx.xml";
		
		config.setJavaDir("src/main/java");
		
		Document doc=XmlJDomUtil.getXmlDocFromFile(tplPath);
		config.setDocument(doc);
		config.createXml(destPath);
	}
}
