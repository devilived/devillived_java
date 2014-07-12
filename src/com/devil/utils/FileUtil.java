package com.devil.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.devil.utils.inferfaces.ITreeIterator;
import com.devil.utils.inferfaces.IVisitFile;

public final class FileUtil {
	public static String getSuffix(String f) {
		int idx = f.lastIndexOf('.');
		if (idx > -1) {
			return f.substring(f.lastIndexOf('.')+1);
		}
		return null;
	}

	/** 把文件中的字符串读出来 **/
	public static String readFile(File f, String cs) {
		if (f.length() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("file is too large");
		}
		ByteArrayOutputStream baos = null;

		BufferedInputStream bis = null;
		try {
			baos = new ByteArrayOutputStream((int) f.length());

			bis = new BufferedInputStream(new FileInputStream(f));
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bis.read(buffer)) > 0) {
				baos.write(buffer, 0, len);
			}
			return new String(baos.toByteArray(), cs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DebugUtil.close(baos, bis);
		}
		return null;
	}

	public static void readLine(File f, String cs, ReadLineCallBack cbk) {
		if (f.length() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("file is too large");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f), cs));
			String tmp;
			while ((tmp = br.readLine()) != null) {
				cbk.onReadLine(br, tmp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DebugUtil.close(br);
		}
	}

	/**
	 * <p>
	 * 把文件从src复制到dest
	 * </p>
	 * 
	 * @param src
	 * @param dest
	 */
	public static void copy(String src, String dest) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(src));
			bos = new BufferedOutputStream(new FileOutputStream(dest));
			byte b[] = new byte[1024];
			while (bis.read(b) > 0) {
				bos.write(b);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DebugUtil.close(bis);
			DebugUtil.close(bos);
		}

	}

	/**
	 * <p>
	 * 递归列出rootpath下边所有文件
	 * </p>
	 * <p>
	 * list all the files of the rootPath cursively according the filter
	 * </p>
	 * 
	 * @param path
	 *            : the root directory
	 * @param filter
	 *            : the filter to filter the files
	 * @return the array of the files of the resutl
	 * @throws IOException
	 */
	public static String[] listDirFiles(String rootPath, final FileFilter filter) throws IOException {
		final List<String> fileList = new ArrayList<String>();

		// a interface to iterate a tree
		ITreeIterator<File> it = new ITreeIterator<File>() {
			@Override
			public List<File> getChildren(File t) {
				if (t == null) {
					return null;
				}
				File[] fileArr = t.listFiles(filter);
				if (CommUtil.isEmpty(fileArr)) {
					return null;
				}
				return Arrays.asList(fileArr);
			}

			@Override
			public void visit(File t) {
				if (t == null) {
					return;
				}
				if (t.isFile()) {
					try {
						fileList.add(t.getCanonicalPath());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
		CommUtil.quickPreIterateTree(new File(rootPath), it);
		return fileList.toArray(new String[0]);
	}

	public static void visitDir(String rootPath, final IVisitFile vf) {
		// a interface to iterate a tree
		ITreeIterator<File> it = new ITreeIterator<File>() {
			@Override
			public List<File> getChildren(File f) {
				if (f == null) {
					return null;
				}
				File[] fileArr = f.listFiles();
				if (CommUtil.isEmpty(fileArr)) {
					return null;
				}
				return Arrays.asList(fileArr);
			}

			@Override
			public void visit(File f) {
				if (f == null) {
					return;
				}
				vf.visit(f);
			}
		};
		CommUtil.quickPreIterateTree(new File(rootPath), it);
	}

	public static void replaceStr(File f, String old, String replace, String charset) {
		String content = readFile(f, charset);
		if (content == null || content.length() == 0) {
			return;
		}
		content = content.replace(old, replace);
		File tmpFile = new File(f.getAbsolutePath() + ".tmp");
		BufferedWriter bw = null;
		try {
			FileOutputStream fos = new FileOutputStream(tmpFile, false);
			bw = new BufferedWriter(new OutputStreamWriter(fos, charset));
			bw.write(content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DebugUtil.close(bw);
		}
		f.delete();
		tmpFile.renameTo(f);
	}

	public static File[] listFiles(File rootDir, final String... sufix) {
		return rootDir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				for (String s : sufix) {
					if (name.toLowerCase().endsWith(s.toLowerCase())) {
						return true;
					}
				}
				return false;
			}
		});
	}

	public static interface ReadLineCallBack {
		public void onReadLine(BufferedReader br, String str) throws Exception;
	}
}
