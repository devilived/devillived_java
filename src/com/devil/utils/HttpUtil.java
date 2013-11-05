package com.devil.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.devil.exceptions.CodeException;
import com.devil.utils.DebugUtil;
import com.devil.utils.Util;

public class HttpUtil {
	private static final boolean DEBUG = true;

	static {
		System.setProperty("http.keepAlive", "true");
		System.setProperty("http.maxConnections", "5");
	}

	public static HttpResponse post(String url, Map<String, String> params,
			Map<String, String> customHttpHeader) {
		return http("POST", url, params, customHttpHeader);
	}

	public static HttpResponse get(String url, Map<String, String> params,
			Map<String, String> customHttpHeader) {
		return http("GET", url, params, customHttpHeader);
	}

	public static InputStream postInputSteam(String url,
			Map<String, String> params, Map<String, String> customHttpHeader) {
		return httpInputStream("POST", url, params, customHttpHeader);
	}

	public static InputStream getInputSteam(String url,
			Map<String, String> params, Map<String, String> customHttpHeader) {
		return httpInputStream("GET", url, params, customHttpHeader);
	}

	private static InputStream httpInputStream(String method, String urlStr,
			Map<String, String> params, Map<String, String> customHttpHeader) {
		HttpURLConnection conn = null;
		Reader reader = null;
		StringBuilder sb = new StringBuilder();
		if (params != null && params.size() > 0) {
			for (Entry<String, String> entry : params.entrySet()) {
				sb.append(entry.getKey() + "=" + entry.getValue()).append("&");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		try {
			if (method == null || "GET".equalsIgnoreCase(method)
					&& sb.length() > 0) {
				urlStr = urlStr + "?" + sb.toString();
				URL url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				if (DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + method + "->" + urlStr);
					logReqHead(conn);
				}
			} else {
				URL url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
			}
			conn.setRequestMethod(method);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			// conn.setRequestProperty("Referer",
			// "http://localhost:8080/Arithmancy/index.html");
			conn.setRequestProperty("Accept-Encoding", "gzip");

			if (customHttpHeader != null && customHttpHeader.size() > 0) {
				for (Entry<String, String> entry : customHttpHeader.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			if ("POST".equalsIgnoreCase(method) && sb.length() > 0) {
				byte[] content = sb.toString().getBytes("utf-8");
				// conn.setRequestProperty("Content-Length",
				// String.valueOf(content.length));

				if (DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + method + "->" + urlStr + "?"
							+ sb);
					logReqHead(conn);
				}
				conn.getOutputStream().write(content);

			} else {
				if (DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + urlStr + "?" + sb);
					logReqHead(conn);
				}
			}

			String respEncoding = conn.getContentEncoding();
			InputStream is = null;
			if ("gzip".equalsIgnoreCase(respEncoding)) {
				is = new GZIPInputStream(conn.getInputStream());
			} else {
				is = conn.getInputStream();
			}
			return is;
		} catch (IOException e) {
			if (e instanceof UnknownHostException) {
				throw new CodeException(CodeException.CODE_NO_NET, e);
			}
			if (conn == null) {
				return null;
			}
			InputStream es = conn.getErrorStream();
			if (es != null) {
				try {
					String errorMsg = Util.readerFromInputStream(es, null);
					System.err.println("http error:" + errorMsg);
					es.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		} finally {
			DebugUtil.close(reader);
		}
		return null;
	}

	public static HttpResponse http(String method, String urlStr,
			Map<String, String> params, Map<String, String> customHttpHeader) {
		HttpURLConnection conn = null;
		Reader reader = null;
		StringBuilder sb = new StringBuilder();
		if (params != null && params.size() > 0) {
			for (Entry<String, String> entry : params.entrySet()) {
				sb.append(entry.getKey() + "=" + entry.getValue()).append("&");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		try {
			if (method == null || "GET".equalsIgnoreCase(method)
					&& sb.length() > 0) {
				urlStr = urlStr + "?" + sb.toString();
				URL url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				if (DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + method + "->" + urlStr);
					logReqHead(conn);
				}
			} else {
				URL url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
			}
			conn.setRequestMethod(method);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			// conn.setRequestProperty("Referer",
			// "http://localhost:8080/Arithmancy/index.html");
			conn.setRequestProperty("Accept-Encoding", "gzip");

			if (customHttpHeader != null && customHttpHeader.size() > 0) {
				for (Entry<String, String> entry : customHttpHeader.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			if ("POST".equalsIgnoreCase(method) && sb.length() > 0) {
				byte[] content = sb.toString().getBytes("utf-8");
				// conn.setRequestProperty("Content-Length",
				// String.valueOf(content.length));

				if (DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + method + "->" + urlStr + "?"
							+ sb);
					logReqHead(conn);
				}
				conn.getOutputStream().write(content);

			} else {
				if (DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + urlStr + "?" + sb);
					logReqHead(conn);
				}
			}

			reader = decodeRespInputStream(conn);
			String msg = Util.readerFromReader(reader);

			Map<String, List<String>> origHeaders = conn.getHeaderFields();
			Map<String, String> headerMap = new HashMap<String, String>();
			for (Entry<String, List<String>> entry : origHeaders.entrySet()) {
				String s = "";
				for (String ss : entry.getValue()) {
					s += ss + "|";
				}
				headerMap.put(entry.getKey(), s.substring(0, s.length() - 1));
			}
			if (DEBUG) {
				System.out.println("-----------------------------");
				for (Entry<String, String> entry : headerMap.entrySet()) {
					System.out.println(entry.getKey() + ":" + entry.getValue());
				}

				System.out.println("HTTP RETURN:\n" + msg);
			}

			return new HttpResponse(msg, headerMap);
		} catch (IOException e) {
			if (e instanceof UnknownHostException) {
				throw new CodeException(CodeException.CODE_NO_NET, e);
			}
			if (conn == null) {
				return null;
			}
			InputStream es = conn.getErrorStream();
			if (es != null) {
				try {
					String errorMsg = Util.readerFromInputStream(es, null);
					System.err.println("http error:" + errorMsg);
					es.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		} finally {
			DebugUtil.close(reader);
		}
		return null;
	}

	/************************************************************************************/
	public static class HttpResponse {
		private String content;
		private Map<String, String> headers;

		public HttpResponse(String content, Map<String, String> headers) {
			this.content = content;
			this.headers = headers;
		}

		public String getContent() {
			return content;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}
	}

	private static Reader decodeRespInputStream(HttpURLConnection conn)
			throws IOException {
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

	private static void logReqHead(HttpURLConnection conn) {
		Map<String, List<String>> headers = conn.getRequestProperties();
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			String s = entry.getKey() + ":";
			for (String ss : entry.getValue()) {
				s += ss + "|";
			}

			System.out.println(s.substring(0, s.length() - 1));
		}
	}
}
