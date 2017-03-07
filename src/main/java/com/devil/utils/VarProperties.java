package com.devil.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map.Entry;

/**
 * root=/a
 * file=${root}/c
 * @author xqs
 */
public class VarProperties extends Properties {
	private static final long serialVersionUID = 1L;
	private static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");
	
	public static void main(String[] args){
		Properties prop=new VarProperties();
		Reader reader = new StringReader("root=aaa\nfile=${root}/test");
		try {
			prop.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(prop.getProperty("file"));
	}
	
	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		super.load(inStream);
		for (Entry<Object, Object> entry : this.entrySet()) {
			entry.setValue(getRealProperty((String) entry.getKey(), (String) entry.getValue()));
		}
	}
	
	@Override
	public synchronized void load(Reader reader) throws IOException {
		super.load(reader);
		for (Entry<Object, Object> entry : this.entrySet()) {
			entry.setValue(getRealProperty((String) entry.getKey(), (String) entry.getValue()));
		}
	}

	private String getRealProperty(String key,String value){
		Matcher matcher = PATTERN.matcher(value);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String matcherKey = matcher.group(1);
			String matchervalue = this.getProperty(matcherKey);
			if (matchervalue != null) {
				matcher.appendReplacement(buffer, matchervalue);
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}
}