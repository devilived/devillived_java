package com.devil.utils;

public class ByteUtil {
	private static final String HEX_CHARS = "0123456789abcdef";

	public static String toHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(HEX_CHARS.charAt(bytes[i] >>> 4 & 0x0F));
			sb.append(HEX_CHARS.charAt(bytes[i] & 0x0F));
		}
		return sb.toString();
	}

}
