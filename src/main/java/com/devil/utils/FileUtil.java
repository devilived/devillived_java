package com.devil.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.devil.utils.IteratorUtil.ITreeIterator;
import com.google.common.io.Files;

public final class FileUtil {
	public static void replaceStr(File f, String old, String replace, String charset) {
		String content=null;
		try {
			content = Files.toString(f, Charset.forName(charset));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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
	
	public static void delete(File file){
		if(file.isDirectory()){
			File[] subfiles=file.listFiles();
			for(File f:subfiles){
				delete(f);
			}
		}else {
			file.delete();
		}
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
}
