package com.devil.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本来参照spring的PathMatchingResourcePatternResolver实现，支持在jar和文件夹中支持类的查询.<br/>
 * 暂不支持vfs<br/>
 * 用于获取指定包名下的所有类名.<br/>
 * 并可设置是否遍历该包名下的子包的类名.<br/>
 * 
 * @author xqs
 * @version Time：2016-06-29
 */
public class PkgUtil {
	private static final Logger logger = LoggerFactory.getLogger(PkgUtil.class);
	/** Separator between JAR URL and file path within the JAR: "!/" */
	public static final String JAR_URL_SEPARATOR = "!/";
	/** URL prefix for loading from the file system: "file:" */
	public static final String FILE_URL_PREFIX = "file:";

	/** URL protocol for an entry from a jar file: "jar" */
	public static final String URL_PROTOCOL_JAR = "jar";

	/** URL protocol for an entry from a zip file: "zip" */
	public static final String URL_PROTOCOL_ZIP = "zip";

	/** URL protocol for an entry from a WebSphere jar file: "wsjar" */
	public static final String URL_PROTOCOL_WSJAR = "wsjar";

	/** URL protocol for an entry from a JBoss jar file: "vfszip" */
	public static final String URL_PROTOCOL_VFSZIP = "vfszip";

	public static void main(String[] args) {
		String pkg = "com.vidmt.api.common.utils";
		Set<Class<?>> set = findClassList(pkg, null);
		for (Class<?> clz : set) {
			System.out.println(clz);
		}
	}

	public static Set<Class<?>> findClassList(String packageName, Class<? extends Annotation> anno) {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			// 按文件的形式去查找
			String strFile = packageName.replaceAll("\\.", "/");
			String pkgdirpattern = strFile.replace("/", File.separator);
			Enumeration<URL> urls = loader.getResources(strFile);
			Set<Class<?>> result = new LinkedHashSet<>();
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (isJarURL(url)) {
					result.addAll(doFindPathMatchingJarResources(url));
				} else {
					try {
						File rootdir = new File(url.toURI());
						result.addAll(retrieveMatchingFiles(rootdir, pkgdirpattern));
					} catch (URISyntaxException e) {
						logger.error("url " + url + "  to file error", e);
					}
				}
			}
			if (anno != null) {
				Set<Class<?>> resultcopy = new LinkedHashSet<>(result.size());
				for (Class<?> clz : result) {
					clz.getAnnotation(anno);
					if (clz.getAnnotation(anno) != null) {
						resultcopy.add(clz);
					}
				}
				return resultcopy;
			}
			return result;
		} catch (IOException e) {
			logger.error("find class error", e);
			return null;
		}
	}

	private static Set<Class<?>> retrieveMatchingFiles(File rootDir, String pkgdirpattern) throws IOException {
		if (!rootDir.exists()) {
			// Silently skip non-existing directories.
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping [" + rootDir.getAbsolutePath() + "] because it does not exist");
			}
			return Collections.emptySet();
		}
		if (!rootDir.isDirectory()) {
			// Complain louder if it exists but is no directory.
			if (logger.isWarnEnabled()) {
				logger.warn("Skipping [" + rootDir.getAbsolutePath() + "] because it does not denote a directory");
			}
			return Collections.emptySet();
		}
		if (!rootDir.canRead()) {
			if (logger.isWarnEnabled()) {
				logger.warn("Cannot search for matching files underneath directory [" + rootDir.getAbsolutePath()
						+ "] because the application is not allowed to read the directory");
			}
			return Collections.emptySet();
		}
		Set<Class<?>> result = new LinkedHashSet<Class<?>>(8);
		doRetrieveMatchingFiles(rootDir, pkgdirpattern, result);
		return result;
	}

	private static Set<Class<?>> doFindPathMatchingJarResources(URL url) throws IOException {
		URLConnection con = url.openConnection();
		JarFile jarFile;
		String jarFileUrl;
		String rootEntryPath;
		boolean newJarFile = false;
		if (con instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			JarURLConnection jarCon = (JarURLConnection) con;
			// ResourceUtils.useCachesIfNecessary(jarCon);
			con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			JarEntry jarEntry = jarCon.getJarEntry();
			rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
		} else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the
			// protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			String urlFile = url.getFile();
			try {
				int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
				if (separatorIndex != -1) {
					jarFileUrl = urlFile.substring(0, separatorIndex);
					rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
					jarFile = getJarFile(jarFileUrl);
				} else {
					jarFile = new JarFile(urlFile);
					jarFileUrl = urlFile;
					rootEntryPath = "";
				}
				newJarFile = true;
			} catch (ZipException ex) {
				return Collections.emptySet();
			}
		}
		try {
			if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
				// Root entry path must end with slash to allow for proper
				// matching.
				// The Sun JRE does not return a slash here, but BEA JRockit
				// does.
				rootEntryPath = rootEntryPath + "/";
			}

			Set<Class<?>> result = new LinkedHashSet<>(8);
			for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryPath = entry.getName();
				if (entryPath.startsWith(rootEntryPath)) {
					if (entryPath.endsWith(".class")) {
						try {
							Class<?> clz = Class.forName(entryPath.replace("/", ".").replace(".class", ""));
							result.add(clz);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return result;
		} finally {
			// Close jar file, but only if freshly obtained -
			// not from JarURLConnection, which might cache the file reference.
			if (newJarFile) {
				jarFile.close();
			}
		}
	}

	/**
	 * Resolve the given jar file URL into a JarFile object.
	 */
	private static JarFile getJarFile(String jarFileUrl) throws IOException {
		if (jarFileUrl.startsWith(FILE_URL_PREFIX)) {
			try {
				URI uri = new URI(jarFileUrl.replace(" ", "%20"));
				return new JarFile(uri.getSchemeSpecificPart());
			} catch (URISyntaxException ex) {
				// Fallback for URLs that are not valid URIs (should hardly ever
				// happen).
				return new JarFile(jarFileUrl.substring(FILE_URL_PREFIX.length()));
			}
		} else {
			return new JarFile(jarFileUrl);
		}
	}

	/**
	 * Determine whether the given URL points to a resource in a jar file, that
	 * is, has protocol "jar", "zip", "vfszip" or "wsjar".
	 * 
	 * @param url
	 *            the URL to check
	 * @return whether the URL has been identified as a JAR URL
	 */
	private static boolean isJarURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol)
				|| URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol));
	}

	/**
	 * Recursively retrieve files that match the given pattern, adding them to
	 * the given result list.
	 * 
	 * @param fullPattern
	 *            the pattern to match against, with prepended root directory
	 *            path
	 * @param dir
	 *            the current directory
	 * @param result
	 *            the Set of matching File instances to add to
	 * @throws IOException
	 *             if directory contents could not be retrieved
	 */
	private static void doRetrieveMatchingFiles(File dir, String pkgdirpattern, Set<Class<?>> result)
			throws IOException {
		File[] dirContents = dir.listFiles();
		if (dirContents == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
			}
			return;
		}
		for (File content : dirContents) {
			if (content.isDirectory()) {
				if (!content.canRead()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Skipping subdirectory [" + dir.getAbsolutePath()
								+ "] because the application is not allowed to read the directory");
					}
				} else {
					doRetrieveMatchingFiles(content, pkgdirpattern, result);
				}
			} else if (content.isFile() && content.getName().endsWith(".class")) {
				String path = content.getAbsolutePath();
				int idx = path.lastIndexOf(pkgdirpattern);
				String className = path.substring(idx).replace(File.separator, ".").replace(".class", "");
				try {
					result.add(Class.forName(className));
				} catch (ClassNotFoundException e) {
					logger.error("init class " + className + " error", e);
				}
			}
		}
	}
}