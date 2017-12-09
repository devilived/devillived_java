package com.devil.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Rename {

	public static void main(String[] args) throws IOException {
		File file = new File("F:\\MUSIC");
		Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String filePath= file.toString();
				if(filePath.contains(" [mqms2].")){
					File newFile = new File(filePath.replace(" [mqms2].", "."));
					file.toFile().renameTo(newFile);
				}else if(filePath.contains(" [mqms].")){
					File newFile = new File(filePath.replace(" [mqms].", "."));
					file.toFile().renameTo(newFile);
				}
				return FileVisitResult.CONTINUE;
			};
		});
		
		System.err.println("=====END==========");
//		Files.walk(file.toPath()).filter(name->name.toString().contains(" [mqms2].")).forEach(name->System.out.println(name));

	}

}
