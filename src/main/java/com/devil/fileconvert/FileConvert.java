package com.devil.fileconvert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.devil.utils.CommUtil;
import com.devil.utils.DebugUtil;
import com.devil.utils.FileUtil;
import com.devil.utils.inferfaces.IVisitFile;

public class FileConvert {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String src="D:\\xampp\\htdocs\\discuz\\source\\plugin\\exam";
		GBK2UTF8(src, null);
	}
	public static void GBK2UTF8(final String srcDir, String ext){
		StringBuilder sb = new StringBuilder(srcDir);
		if(srcDir.endsWith(File.separator)){
			sb.deleteCharAt(srcDir.length()-1);
		}
		final String destDir = sb.append("_utf8").toString();
		
		if(CommUtil.isEmpty(ext)){
			ext = "txt | xml | php | css | htm | html | java ";
		}
		String[] arr = ext.split("\\|");
		
		final Set<String> set = new HashSet<String>();
		for(String s : arr){
			set.add(s.trim());
		}
		
		IVisitFile vf = new IVisitFile() {
			@Override
			public void visit(File f) {
				String fName = f.getName();
				int pos = fName.lastIndexOf('.');
				if(pos<1){
					return;
				}
				
				String ext = fName.substring(pos+1);
				if (f.isFile()) {
					FileWriter fw = null;
					try {
						String srcFilePath = f.getAbsolutePath();
						String destFilePath = srcFilePath.replace(srcDir, destDir);
						File fdest = new File(destFilePath);
						if (!fdest.getParentFile().exists()) {
							fdest.getParentFile().mkdirs();
						}
						if (set.contains(ext)) {
							String s = FileUtil.readFile(f, "GBK");
							fw = new FileWriter(fdest);
							fw.write(s);
						} else {
							FileUtil.copy(srcFilePath, destFilePath);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}finally{
						DebugUtil.close(fw);
					}
				}
			}
		};
		FileUtil.visitDir(srcDir, vf);
	}

}
