package com.devil.utils;

public final class StrUtil {
	public static String lowFirstChar(String src) {
		if (src.length() > 0) {
			src = src.substring(0, 1).toLowerCase() + src.substring(1);
		}
		return src;
	}
	
	/**把一段unicode十六进制字符串转换为普通字符串
	 * "&#x6b22;&#x8fce;&#x5149;&#x4e34;&#x672c;&#x7ad9;&#xff0c;&#x9875;&#x9762;&#x6b63;&#x5728;&#x91cd;&#x65b0;&#x8f7d;&#x5165;&#xff0c;&#x8bf7;&#x7a0d;&#x5019;&#x20;&#x2e;&#x2e;&#x2e;";
	 * 转换为"欢迎光临本站，页面正在重新载入，请稍候 ..."
	 */
	public static String unicode2String(String unicode){
		if(unicode==null){
			return null;
		}
		StringBuffer sb=new StringBuffer();
		String[] arr=unicode.replaceAll("\\s", "").split(";");
		for(String s:arr){
			s=s.replace("&#x", "");
			sb.append((char)(Integer.parseInt(s, 16)));
		}
		
		return sb.toString();
	}

}
