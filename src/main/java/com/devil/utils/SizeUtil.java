package com.devil.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SizeUtil {
	private ByteArrayOutputStream bos;
	private ObjectOutputStream oos;

	public SizeUtil() {
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int size(Object obj) {
		try {
			oos.writeObject(obj);
			int size = bos.size();
			bos.reset();
			return size;
		} catch (Throwable e) {
			e.printStackTrace();
			destroy();
			return -1;
		}
	}

	public void destroy() {
		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
