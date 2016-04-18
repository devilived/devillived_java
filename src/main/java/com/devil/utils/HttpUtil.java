package com.devil.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 1. supports get/post string/inputstream;<br/>
 * 2. support gzip;<br/>
 * 3. check the proxy setting in some mobile which doesn't setting the default
 * proxy;<br/>
 * 
 * @author xingqisheng
 * 
 */
public class HttpUtil {
	private static final int CONN_TIMEOUT = 2 * 60 * 1000;
	private static final int READ_TIMEOUT = 2 * 60 * 1000;
	private static final Charset CHARSET = StandardCharsets.UTF_8;

	public static String getStr(String url, String... params) throws HttpException {
		HttpGet get = buildGet(url, params);
		return httpStr(get);
	}

	// public static InputStream getInputStream(String url, String... params) {
	// HttpGet get = buildGet(url, params);
	// return httpInputStream(get);
	// }

	public static String postStr(String url, String... params) throws HttpException {
		HttpPost post = buildPost(url, params);
		return httpStr(post);
	}

	/**
	 * 用GET方法下载文件
	 * 
	 * @param path
	 *            如果最后的后缀是/或者\,那么就认为是文件夹，否则认为是文件名
	 */
	public static File download(String path, String url, String... params) throws HttpException, IOException {
		HttpGet get = buildGet(url, params);
		return httpFile(get, path);
	}

	public static String submitFile(String url, String name, File f, String... params) throws HttpException {
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		// builder.setCharset(Charset.forName("uft-8"));//设置请求的编码格式
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);// 设置浏览器兼容模式
		for (int i = 0; i < params.length - 1; i += 2) {
			if (!CommUtil.isEmpty(params[i + 1])) {
				String value = params[i + 1];
				builder.addTextBody(params[i], value);
			}
		}
		builder.addBinaryBody(name, f);

		HttpPost post = new HttpPost(url);
		post.setEntity(builder.build());
		return httpStr(post);
	}

	public static String postStream(String url, String body) throws HttpException {
		HttpPost post = new HttpPost(url);
		StringEntity entiry = new StringEntity(body, "UTF-8");
		post.setEntity(entiry);
		post.setHeader("Content-Type", "text/xml;charset=utf-8");
		return httpStr(post);
	}

	// //////////////////////////////////////////////////////////////
	private static String httpStr(HttpUriRequest req) throws HttpException {
		CloseableHttpResponse resp = null;
		try {
			// and then from inside some thread executing a method
			// HttpResponse resp = executeWithCheckProxy(req);
			CloseableHttpClient client = getHttpClient();
			resp = client.execute(req);
			if (resp == null || resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				req.abort();
				throw new HttpException("url:" + req.getURI() + "->\n reponse:" + resp.getStatusLine().toString());
			}

			HttpEntity entity = resp.getEntity();
			InputStream stream = getInputStream(entity);
			Charset cs = Charset.forName("UTF-8");
			String csStr = getCharSet(resp);
			if (csStr != null) {
				cs = Charset.forName(csStr);
			}
			String result = getString(stream, cs);
			EntityUtils.consume(entity);
			return result;
		} catch (IOException e) {
			throw new HttpException("HTTP出错", e);
		} finally {
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static File httpFile(HttpUriRequest req, String path) throws IOException, HttpException {
		CloseableHttpResponse resp = null;
		try {
			CloseableHttpClient client = getHttpClient();
			resp = client.execute(req);
			if (resp == null || resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				req.abort();
				throw new IOException("url:" + req.getURI() + "->\n reponse:" + resp.getStatusLine().toString());
			}

			HttpEntity entity = resp.getEntity();
			InputStream is = getInputStream(entity);
			File file;
			if (path.endsWith("\\") || path.endsWith("/")) {
				String fileName = getDefaultFileName(resp);
				if (fileName != null) {
					file = new File(path, fileName);
				} else {
					String uriPath = req.getURI().getPath();
					file = new File(path, uriPath.substring(uriPath.lastIndexOf("/")));
				}

			} else {
				file = new File(path);
			}
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			BufferedOutputStream bos = null;
			try {
				byte[] buff = new byte[1024 * 1024];
				bos = new BufferedOutputStream(new FileOutputStream(file));
				int len;
				while ((len = is.read(buff)) > 0) {
					bos.write(buff, 0, len);
				}
			} finally {
				if (bos != null) {
					bos.close();
				}
			}

			EntityUtils.consume(entity);
			return file;
		} finally {
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static HttpGet buildGet(String url, String... params) {
		if (params != null && params.length > 0) {
			StringBuilder sb = new StringBuilder(url);
			if (!url.contains("?")) {
				sb.append("?");
			}
			if (!url.endsWith("?") && !url.endsWith("&")) {
				sb.append("&");
			}
			for (int i = 0; i < params.length - 1; i += 2) {
				if (!CommUtil.isEmpty(params[i + 1])) {
					String value = params[i + 1];
					sb.append(params[i]).append("=").append(value).append("&");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			url = sb.toString();
		}
		HttpGet get = new HttpGet(url);
		get.setConfig(conf);
		return get;
	}

	private static HttpPost buildPost(String url, String... params) {
		HttpPost post = new HttpPost(url);
		if (params != null && params.length > 0) {
			List<BasicNameValuePair> nameValueList = new ArrayList<BasicNameValuePair>();
			for (int i = 0; i < params.length; i += 2) {
				if (!CommUtil.isEmpty(params[i + 1])) {
					String value = params[i + 1];
					BasicNameValuePair pair = new BasicNameValuePair(params[i], value);
					nameValueList.add(pair);
				}
			}
			post.setEntity(new UrlEncodedFormEntity(nameValueList, CHARSET));
		}
		post.setConfig(conf);
		return post;
	}

	private static final String getDefaultFileName(HttpResponse resp) {
		try {
			Header header = resp.getFirstHeader("Content-Disposition");
			String contentType = header.getValue();
			int idx = contentType.indexOf("filename=");
			return contentType.substring(idx + "filename=".length());
		} catch (Throwable e) {
			return null;
		}
	}

	private static final String getCharSet(HttpResponse resp) {
		Header header = resp.getFirstHeader("Content-Type");
		if (header == null) {
			return null;
		}
		String contentType = header.getValue();
		for (String element : contentType.split(";")) {
			String[] keyValue = element.split("=");
			if (keyValue[0] != null && keyValue[0].contains("charset")) {
				return keyValue[1];
			}
		}
		return null;
	}

	private static InputStream getInputStream(HttpEntity entity) throws IOException {
		InputStream stream = entity.getContent();
		if (entity.getContentEncoding() != null && entity.getContentEncoding().getValue() != null
				&& "gzip".equalsIgnoreCase(entity.getContentEncoding().getValue())) {
			stream = new GZIPInputStream(stream);
		}
		return stream;
	}

	private static String getString(InputStream inputStream, Charset cs) throws IOException {
		BufferedReader reader = null;
		if (cs != null) {
			reader = new BufferedReader(new InputStreamReader(inputStream, cs));
		} else {
			reader = new BufferedReader(new InputStreamReader(inputStream));
		}
		StringBuffer buffer = new StringBuffer();
		String tmp = null;
		while ((tmp = reader.readLine()) != null) {
			buffer.append(tmp);
		}
		reader.close();
		return buffer.toString();
	}

	private static CloseableHttpClient customerHttpClient;

	private static RequestConfig conf = RequestConfig.custom().setSocketTimeout(CONN_TIMEOUT)
			.setConnectTimeout(CONN_TIMEOUT).setConnectionRequestTimeout(READ_TIMEOUT).build();

	private static synchronized CloseableHttpClient getHttpClient() {
		if (null == customerHttpClient) {
			String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
//			CookieStore cookieStore = new BasicCookieStore();
//			String[][] cookies = { { "um_lang", "zh", "mobile.umeng.com" } };
//			for (String[] cookiestr : cookies) {
//				BasicClientCookie cookie = new BasicClientCookie(cookiestr[0], cookiestr[1]);
//				cookie.setDomain(cookiestr[2]);
//				cookie.setPath("/");
//				cookieStore.addCookie(cookie);
//			}

			customerHttpClient = HttpClients.custom().setDefaultRequestConfig(conf)// .setDefaultCookieStore(cookieStore)
					.setUserAgent(UA).build();

		}
		return customerHttpClient;
	}
}
