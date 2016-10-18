package com.devil.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public final class StrUtil {
	private static final Joiner defaultJoiner = Joiner.on(',').skipNulls();
	private static final Splitter defaultSplit = Splitter.on(',').omitEmptyStrings().trimResults();

	public static String join(Iterable<?> src) {
		return defaultJoiner.join(src);
	}
	public static String join(Object[] src) {
		return defaultJoiner.join(src);
	}

	public static String join(String delimeter, Iterable<?> src) {
		return Joiner.on(delimeter).skipNulls().join(src);
	}

	public static String join(String delimeter, Object[] src) {
		return Joiner.on(delimeter).skipNulls().join(src);
	}
	public List<String> split(CharSequence str) {
		return defaultSplit.splitToList(str);
	}

	public List<String> split(CharSequence str, String delimiString) {
		return Splitter.on(delimiString).omitEmptyStrings().trimResults().splitToList(str);
	}

	public static byte[] bytesutf8(String s) {
		return s.getBytes(StandardCharsets.UTF_8);
	}

	public static String newutf8(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static byte[] getStrByte(String s, String cs) {
		return s.getBytes(Charset.forName(cs));
	}

	public static String newString(byte[] bytes, String cs) {
		return new String(bytes, Charset.forName(cs));
	}

	public static String lowFirstChar(String src) {
		if (src.length() > 0) {
			src = src.substring(0, 1).toLowerCase() + src.substring(1);
		}
		return src;
	}
	public static String subString(String src,String start,String end){
		int startIdx=0,endIdx=src.length();
		if(start!=null){
			startIdx=src.indexOf(start);
		}
		if(end!=null){
			endIdx=src.indexOf(end);
		}
		return src.substring(startIdx,endIdx);
	}
}
