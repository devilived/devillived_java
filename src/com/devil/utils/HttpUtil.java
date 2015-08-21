package com.devil.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

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
	private static final int CONN_TIMEOUT = 10 * 60 * 1000;
	private static final int READ_TIMEOUT = 10 * 60 * 1000;
	private static final boolean USE_GZIP = false;
	private static final String DEFALUT_CS = "UTF-8";

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

	public static File getFile(String dir, boolean isDir, String url, String... params) throws HttpException {
		HttpGet get = buildGet(url, params);
		return httpFile(get, dir, true);
	}

	public static File getFile(String path, String url, String... params) throws HttpException {
		HttpGet get = buildGet(url, params);
		return httpFile(get, path, false);
	}

	public static String postFile(String url, String name, File f) throws HttpException {
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();  
//      builder.setCharset(Charset.forName("uft-8"));//设置请求的编码格式  
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//设置浏览器兼容模式 

		builder.addBinaryBody(name, f);  

		HttpPost post = new HttpPost(url);
		post.setEntity(builder.build());
		return httpStr(post);
	}

	// //////////////////////////////////////////////////////////////
	private static String httpStr(HttpUriRequest req) throws HttpException {
		if (USE_GZIP) {
			req.addHeader("Accept-Encoding", "gzip");
		}
		return http(req, new IHttpCallback<String>() {

			@Override
			public String onGotResp(HttpResponse resp) throws IOException {
				InputStream stream = getInputStream(resp.getEntity());
				Charset cs = Charset.forName("UTF-8");
				String csStr = getCharSet(resp);
				if (csStr != null) {
					cs = Charset.forName(csStr);
				}
				return getString(stream, cs);
			}
		});
	}

	private static File httpFile(final HttpUriRequest req, final String path, final boolean pathIsDir) throws HttpException {
		if (USE_GZIP) {
			req.addHeader("Accept-Encoding", "gzip");
		}
		return http(req, new IHttpCallback<File>() {

			@Override
			public File onGotResp(HttpResponse resp) throws IOException {
				InputStream is = getInputStream(resp.getEntity());
				File file;
				if (!isDir) {
					file = new File(path);
				} else {
					String fileName = getDefaultFileName(resp);
					if (fileName != null) {
						file = new File(path, fileName);
					} else {
						String uriPath = req.getURI().getPath();
						file = new File(path, uriPath.substring(uriPath.lastIndexOf("/")));
					}
				}
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				BufferedOutputStream bos = null;
				try {
					byte[] buff = new byte[1024*1024];
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
				return file;
			}
		});
	}

	// ///////////////////////////////////////////////////////////
	private static <RtnType> RtnType http(HttpUriRequest req, IHttpCallback<RtnType> callback) throws HttpException {
		// and then from inside some thread executing a method
		HttpEntity entity = null;
		try {
			// and then from inside some thread executing a method
			// HttpResponse resp = executeWithCheckProxy(req);
			HttpClient client = getHttpClient();
			HttpResponse resp = client.execute(req);
			if (resp == null || resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				req.abort();
				throw new HttpException("url:" + req.getURI() + "->\n reponse:" + resp.getStatusLine().toString());
			}
			return callback.onGotResp(resp);
		} catch (IOException e) {
			e.printStackTrace();
			req.abort();
		} finally {
			// be sure the connection is released back to the connection manager
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException e) {
					e.printStackTrace();
					req.abort();
				}
			}
		}
		return null;
	}

	private static interface IHttpCallback<RtnType> {
		public RtnType onGotResp(HttpResponse response) throws IOException;
	}

	// ////////////////////////////////////////////
	private static HttpGet buildGet(String url, String... params) {
		if (params != null && params.length > 0) {
			StringBuilder sb = new StringBuilder(url).append("?");
			for (int i = 0; i < params.length - 1; i += 2) {
				if (!CommUtil.isEmpty(params[i + 1])) {
					try {
						String value = URLEncoder.encode(params[i + 1], "UTF-8");
						sb.append(params[i]).append("=").append(value).append("&");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			url = sb.toString();
		}
		return new HttpGet(url);
	}

	private static HttpPost buildPost(String url, String... params) {
		HttpPost post = new HttpPost(url);
		if (params != null && params.length > 0) {
			List<BasicNameValuePair> nameValueList = new ArrayList<BasicNameValuePair>();
			for (int i = 0; i < params.length; i += 2) {
				if (!CommUtil.isEmpty(params[i + 1])) {
					BasicNameValuePair pair = new BasicNameValuePair(params[i], params[i + 1]);
					nameValueList.add(pair);
				}
			}
			try {
				post.setEntity(new UrlEncodedFormEntity(nameValueList, DEFALUT_CS));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
		return post;
	}

	// ///////////////////check wheth need proxy
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

	private static final String CHARSET = HTTP.UTF_8;
	private static HttpClient customerHttpClient;

	@SuppressWarnings("deprecation")
	public static synchronized HttpClient getHttpClient() {
		if (null == customerHttpClient) {
			HttpParams params = new BasicHttpParams();
			// 设置一些基本参数
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET);
			// 禁止先发送请求头进行测试。
			HttpProtocolParams.setUseExpectContinue(params, false);
			// HttpProtocolParams.setUserAgent(params,
			// "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
			// +
			// "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			// 超时设置
			/* 从连接池中取连接的超时时间 */
			// ConnManagerParams.setTimeout(params, 1000);
			/* 连接超时 */
			HttpConnectionParams.setConnectionTimeout(params, CONN_TIMEOUT);
			/* 请求超时 */
			HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);

			// 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			// 使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			customerHttpClient = new DefaultHttpClient(conMgr, params);
		}
		return customerHttpClient;
	}
}
