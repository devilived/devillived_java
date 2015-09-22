package com.devil.utils;

public class Base64Coder {
	private static char[] byte2CharDict = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
	private static byte[] char2Byte = new byte[256];
	static {
		for (int i = 0; i < 256; i++){
			char2Byte[i] = -1;
		}
		for (int i = 'A'; i <= 'Z'; i++){
			char2Byte[i] = (byte) (i - 'A');
		}
		for (int i = 'a'; i <= 'z'; i++){
			char2Byte[i] = (byte) (26 + i - 'a');
		}
		for (int i = '0'; i <= '9'; i++){
			char2Byte[i] = (byte) (52 + i - '0');
		}
		char2Byte['+'] = 62;
		char2Byte['/'] = 63;
	}

	/**
	 * 将原始数据编码为base64编码
	 */
	static public String encode(byte[] plainData) {
		char[] out = new char[((plainData.length + 2) / 3) * 4];

		for (int i = 0, index = 0; i < plainData.length; i += 3, index += 4) {
			boolean quad = false;
			boolean trip = false;
			int val = (0xFF & (int) plainData[i]);//取低8位
			val <<= 8;
			if ((i + 1) < plainData.length) {
				val |= (0xFF & (int) plainData[i + 1]);
				trip = true;
			}
			val <<= 8;
			if ((i + 2) < plainData.length) {
				val |= (0xFF & (int) plainData[i + 2]);
				quad = true;
			}
			out[index + 3] = byte2CharDict[(quad ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 2] = byte2CharDict[(trip ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 1] = byte2CharDict[val & 0x3F];
			val >>= 6;
			out[index + 0] = byte2CharDict[val & 0x3F];
		}
		return new String(out);
	}

	/**
	 * 将base64编码的数据解码成原始数据
	 */
	static public byte[] decode(String cyperText) {
		char[] cyper=cyperText.toCharArray();
		int len = ((cyper.length + 3) / 4) * 3;
		if (cyper.length > 0 && cyper[cyper.length - 1] == '='){
			--len;
		}
		if (cyper.length > 1 && cyper[cyper.length - 2] == '='){
			--len;
		}
		byte[] out = new byte[len];
		
		int shift = 0;
		int accum = 0;
		int index = 0;
		for (int ix = 0; ix < cyper.length; ix++) {
			int value = char2Byte[cyper[ix] & 0xFF];
			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) ((accum >> shift) & 0xff);
				}
			}
		}
		if (index != out.length)
			throw new RuntimeException("miscalculated data length!");
		return out;
	}
}
