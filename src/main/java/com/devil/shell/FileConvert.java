package com.devil.shell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import com.devil.utils.CommUtil;
import com.devil.utils.DebugUtil;

public class FileConvert {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String src = "D:\\xampp\\htdocs\\discuz\\source\\plugin\\exam";
		try {
			GBK2UTF8(new File(src), null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void GBK2UTF8(final File srcDir, String ext) throws IOException {
		final File desrDir = new File(srcDir.getParentFile(), srcDir.getName() + "_utf8");

		if (CommUtil.isEmpty(ext)) {
			ext = "txt | xml | php | css | htm | html | java ";
		}
		String[] arr = ext.split("\\|");

		final Set<String> set = new HashSet<String>();
		for (String s : arr) {
			set.add(s.trim());
		}

		Files.walkFileTree(srcDir.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File f = file.toFile();
				String fName = f.getName();
				int pos = fName.lastIndexOf('.');
				if (pos < 1) {
					return super.visitFile(file, attrs);
				}

				String ext = fName.substring(pos + 1);
				FileWriter fw = null;
				try {
					Path relPath = srcDir.toPath().relativize(file);
					File fdest = new File(desrDir, relPath.toString());
					if (!fdest.getParentFile().exists()) {
						fdest.getParentFile().mkdirs();
					}
					if (set.contains(ext)) {
						byte[] gbkbytes = Files.readAllBytes(f.toPath());
						String s = new String(gbkbytes, "GBK");
						fw = new FileWriter(fdest);
						fw.write(s);
					} else {
						Files.copy(f.toPath(), fdest.toPath());
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					CommUtil.close(fw);
				}
				return super.visitFile(file, attrs);
			}
		});
	}

}
