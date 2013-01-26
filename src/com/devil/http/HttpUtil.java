package com.devil.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

/**http post读出字符*/
public class HttpUtil {
	public static Reader post(String urlStr, Map<String, String> params,
			Map<String, String> customHttpHeader) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
		conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
		conn.setRequestProperty("Accept-Encoding", "gzip");
		if (customHttpHeader != null && customHttpHeader.size() > 0) {
			for (Entry<String, String> entry : customHttpHeader.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}

//		logRespHead(conn);
		if (params != null && params.size() > 1) {
			StringBuilder sb = new StringBuilder();
			for (Entry<String, String> entry : params.entrySet()) {
				sb.append(entry.getKey() + "=" + entry.getValue()).append("&");
			}
			sb.deleteCharAt(sb.length() - 1);
			conn.getOutputStream().write(sb.toString().getBytes("utf-8"));
		}

		System.out.println("======after post============");
		logRespHead(conn);
		
		String respEncoding = conn.getContentEncoding();
		InputStream is = null;
		if ("gzip".equalsIgnoreCase(respEncoding)) {
			is = new GZIPInputStream(conn.getInputStream());
		} else {
			is = conn.getInputStream();
		}

		String charset = null;
		String contentType = conn.getContentType();
		if (contentType != null) {
			String[] arr = contentType.replace(" ", "").split(";");
			for (String s : arr) {
				if (s.startsWith("charset=")) {
					charset = s.split("=", 2)[1];
				}
			}
		}

		if (charset != null) {
			return new InputStreamReader(is, charset);
		} else {
			return new InputStreamReader(is);
		}
	}

	public static void logRespHead(HttpURLConnection conn) {
		Map<String, List<String>> headers = conn.getHeaderFields();
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			String s = entry.getKey() + ":    ";
			for (String ss : entry.getValue()) {
				s += ss + "|";
			}

			System.out.println(s.substring(0, s.length() - 1));
		}
	}
}
