package com.devil.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PkgUtil {

	public static void main(String[] args) {
		String packageName = "com.mysql.jdbc";
		List<String> classNames = getClassName(packageName, true);
		if (classNames != null) {
			for (String className : classNames) {
				System.out.println(className);
			}
		}
	}

	/**
	 * 获取某包下所有类
	 * 
	 * @param pkgName
	 *            包名
	 * @param subPkg
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	public static List<String> getClassName(String pkgName, boolean subPkg) {
		List<String> fileNames = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String packagePath = pkgName.replace(".", "/");
		URL url = loader.getResource(packagePath);
		try {
			if (url != null) {
				String type = url.getProtocol();
				if (type.equals("file")) {
					fileNames = getClassNameByFile(url.getPath(), null, subPkg);
				} else if (type.equals("jar")) {

					fileNames = getClassNameByJar(url.getPath(), subPkg);

				}
			} else if (loader instanceof URLClassLoader) {
				fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, subPkg);
			} else {
				throw new IllegalStateException("invalid class loader");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileNames;
	}

	/**
	 * 从项目文件获取某包下所有类
	 * 
	 * @param filePath
	 *            文件路径
	 * @param className
	 *            类名集合
	 * @param childPackage
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	private static List<String> getClassNameByFile(String filePath, List<String> className, boolean childPackage) {
		List<String> myClassName = new ArrayList<String>();
		File file = new File(filePath);
		File[] childFiles = file.listFiles();
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				if (childPackage) {
					myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));
				}
			} else {
				String childFilePath = childFile.getPath();
				if (childFilePath.endsWith(".class")) {
					childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath
							.lastIndexOf("."));
					childFilePath = childFilePath.replace("\\", ".");
					myClassName.add(childFilePath);
				}
			}
		}

		return myClassName;
	}

	/**
	 * 从jar获取某包下所有类
	 * 
	 * @param jarPath
	 *            jar文件路径
	 * @param childPackage
	 *            是否遍历子包
	 * @return 类的完整名称
	 * @throws IOException
	 */
	private static List<String> getClassNameByJar(String jarPath, boolean childPackage) throws IOException {
		List<String> clzList = new ArrayList<String>();
		String[] jarInfo = jarPath.split("!");
		String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
		String pkgPath = jarInfo[1].substring(1);
		JarFile jarFile = new JarFile(jarFilePath);
		Enumeration<JarEntry> entrys = jarFile.entries();
		while (entrys.hasMoreElements()) {
			JarEntry jarEntry = entrys.nextElement();
			String entryName = jarEntry.getName();
			if (entryName.endsWith(".class")) {
				if (childPackage) {
					if (entryName.startsWith(pkgPath)) {
						entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
						clzList.add(entryName);
					}
				} else {
					int index = entryName.lastIndexOf("/");
					String curPkgPath = (index > -1 ? entryName.substring(0, index) : entryName);
					if (curPkgPath.equals(pkgPath)) {
						entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
						clzList.add(entryName);
					}
				}
			}
		}
		jarFile.close();
		return clzList;
	}

	/**
	 * 从所有jar中搜索该包，并获取该包下所有类
	 * 
	 * @param urls
	 *            URL集合
	 * @param pkgPath
	 *            包路径
	 * @param childPackage
	 *            是否遍历子包
	 * @return 类的完整名称
	 * @throws IOException
	 */
	private static List<String> getClassNameByJars(URL[] urls, String pkgPath, boolean childPackage) throws IOException {
		List<String> clzList = new ArrayList<String>();
		if (urls != null) {
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				String urlPath = url.getPath();
				// 不必搜索classes文件夹
				if (urlPath.endsWith("classes/")) {
					continue;
				}
				String jarPath = urlPath + "!/" + pkgPath;
				clzList.addAll(getClassNameByJar(jarPath, childPackage));
			}
		}
		return clzList;
	}
}
