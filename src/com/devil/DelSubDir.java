package com.devil;

import java.io.File;

import com.devil.utils.FileUtil;
import com.devil.utils.inferfaces.IVisitFile;

public class DelSubDir {
	public static void main(String[] args) {
		String path = "C:\\Users\\Administrator\\Desktop\\apkUtil-v1.1.0 src";
		String fileName = ".*\\.svn";
		DelSubDir.delSubFile(path, fileName);
	}

	public static void delSubFile(String path, final String dirRex) {
		IVisitFile vf = new IVisitFile() {
			@Override
			public void visit(File f) {
				String fName = f.getName();
				if (fName.matches(dirRex)) {
					deleteAll(f);
				}
			}
		};
		FileUtil.visitDir(path, vf);
	}

	public static void deleteAll(File f) {
		if (f.isFile()) {
			f.delete();
		} else if (f.isDirectory()) {
			File[] fileArr = f.listFiles();
			for (File ff : fileArr) {
				deleteAll(ff);
			}
			f.delete();
		}
	}
}
