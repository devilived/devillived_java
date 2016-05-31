package com.devil.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

public class CommUtil {
	private static final boolean DEBUG = false;
	private static final String TAG = "util";

	/** 主要用来判断常用的容器字符串是否为空 **/
	public static boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		}
		if (o instanceof String) {
			return ((String) o).trim().length() == 0;
		} else if (o instanceof CharSequence) {
			return ((CharSequence) o).length() == 0;
		} else if (o instanceof Collection) {
			return ((Collection<?>) o).size() == 0;
		} else if (o instanceof Map) {
			return ((Map<?, ?>) o).size() == 0;
		} else if (o.getClass().isArray()) {
			return Array.getLength(o) == 0;
		}
		return false;
	}

	public static boolean isSubClass(Class<?> clz, Class<?> supperClz) {
		return supperClz.isAssignableFrom(clz);
	}

	public static String byte2HexStr(byte[] arr) {
		StringBuffer sb = new StringBuffer(arr.length * 2);
		for (int i = 0; i < arr.length; ++i) {
			String hex = Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1, 3);
			sb.append(hex);
		}
		return sb.toString();
	}

	public static String md5(byte[] src) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(src);
			byte b[] = md.digest();
			return byte2HexStr(b);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String encBase64Url(byte[] bytes) {
		return Base64.encodeBase64URLSafeString(bytes);
	}

	public static byte[] decBase64Url(String str) {
		return Base64.decodeBase64(str);
	}

	public static String urlEnc(String s, String cs) {
		try {
			return URLEncoder.encode(s, cs);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String urlDec(String s, String cs) {
		try {
			return URLDecoder.decode(s, cs);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	

	public static String fmtException(Throwable e) {
		Writer sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		Throwable cause = e.getCause();
		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		String result = sw.toString();
		pw.close();
		return result;
	}

	public static int randInt(int len) {
		if (len > 9) {
			throw new IllegalArgumentException("the length of 'len' must be less than 9");
		}
		Random random = new Random();
		int highestBit = random.nextInt(9) + 1;

		int highestUnit = (int) Math.pow(10, len - 1);
		int laterNum = (int) Math.floor(random.nextInt(highestUnit));
		return highestBit * highestUnit + laterNum;
	}

	public static String randString(int len) {
		char[] dest = new char[len];
		Random random = new Random();
		for (int i = 0; i < len; i++) {
			int r = random.nextInt(36);
			if (r < 10) {
				dest[i] = (char) ('0' + r);
			} else {
				dest[i] = (char) ('a' + r - 10);
			}
		}
		return new String(dest);
	}

	public static String readerFromInputStream(InputStream is, String charset) throws IOException {
		if (is == null) {
			return null;
		}
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			if (charset == null) {
				isr = new InputStreamReader(is);
			} else {
				isr = new InputStreamReader(is, charset);
			}

			br = new BufferedReader(isr);

			String tmp = null;
			while ((tmp = br.readLine()) != null) {
				sb.append(tmp);
			}
		} finally {
			close(br);
		}
		return sb.toString();
	}

	public static String readerFromReader(Reader reader) throws IOException {
		if (reader == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(reader);
		String tmp = null;
		while ((tmp = br.readLine()) != null) {
			sb.append(tmp);
		}
		return sb.toString();
	}

	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String fmtDate(Date date) {
		return df.format(date);
	}

	public static Date parseDate(String s) {
		try {
			return df.parse(s);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static String fmtSize(Integer size) {
		if (size < 1024) {
			return size + "B";
		}
		if (size < 1020 * 1024) {
			return String.format("%.2fK", size * 1.0 / 1024);
		}
		if (size < 1024 * 1024 * 1024) {
			return String.format("%.2fM", size * 1.0 / 1024 / 1024);
		}
		return String.format("%.2fG", size * 1.0 / 1024 / 1024 / 1024);
	}

	public static void close(Object... objs) {
		if (isEmpty(objs)) {
			return;
		}
		try {
			for (Object obj : objs) {
				if (obj == null) {
					continue;
				}
				if (obj instanceof InputStream) {
					((InputStream) obj).close();
				} else if (obj instanceof OutputStream) {
					((OutputStream) obj).close();
				} else if (obj instanceof Reader) {
					((Reader) obj).close();
				} else if (obj instanceof Writer) {
					((Writer) obj).close();
				} else {
					try {
						obj.getClass().getMethod("close").invoke(obj);
					} catch (Throwable e) {
						throw new IllegalArgumentException(
								"only inputstream/outputstream/reader/writer and others that has 'close()' method can be the param"
										+ obj.getClass());
					}
				}

			}
		} catch (IOException e) {
			throw new IllegalStateException("close resource error", e);
		}
	}
}
