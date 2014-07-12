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
	public static <T> void printArr(T[] arr) {
		if (arr != null) {
			for (T t : arr) {
				System.out.println(t);
			}
		}
	}

	public static void UITest(JComponent comp) {
		JFrame f = new JFrame();
		f.setContentPane(comp);
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.pack();
		UIUtil.positionFrameOnScreen(f, 0.5D, 0.5D);

		f.setVisible(true);
	}

	public static void close(Object... objs) {
		if (CommUtil.isEmpty(objs)) {
			return;
		}
		try {
			for (Object obj : objs) {
				if (obj instanceof InputStream) {
					((InputStream) obj).close();
				} else if (obj instanceof OutputStream) {
					((OutputStream) obj).close();
				} else if (obj instanceof Reader) {
					((Reader) obj).close();
				} else if (obj instanceof Writer) {
					((Writer) obj).close();
				} else {
					throw new IllegalArgumentException(
							"only inputstream/outputstream/reader/writer can be the param");
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
