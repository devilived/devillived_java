package com.devil.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public final class DebugUtil {
	public static <T> void printArr(T[] arr){
		if(arr!=null){
			for(T t:arr){
				System.out.println(t);
			}
		}
	}
	
	public static void UITest(JComponent comp){
		JFrame f=new JFrame();
		f.setContentPane(comp);
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.pack();
		UIUtil.positionFrameOnScreen(f,0.5D,0.5D);
		
		
		f.setVisible(true);
	}
	
	public static void close(InputStream... isArr){
		if(Util.isNull(isArr)){
			return;
		}
		try {
			for(InputStream is: isArr){
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void close(OutputStream... osArr){
		if(Util.isNull(osArr)){
			return;
		}
		try {
			for(OutputStream os: osArr){
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(Reader... readerArr){
		if(Util.isNull(readerArr)){
			return;
		}
		try {
			for(Reader reader: readerArr){
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(Writer... writerArr){
		if(Util.isNull(writerArr)){
			return;
		}
		try {
			for(Writer writer: writerArr){
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
