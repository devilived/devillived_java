package com.devil.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public final class DebugUtil {
	public static String sortToString(Collection<?> collection) {
		List<String> list = new ArrayList<>(collection.size());
		Iterator<?> it = collection.iterator();
		while (it.hasNext()) {
			list.add(it.next().toString());
		}
		Collections.sort(list);
		return list.toString();
	}

	public static String sortToString(Object... arr) {
		Arrays.sort(arr);
		return Arrays.toString(arr);
	}

	public static <T> void printAsLine(T[] arr) {
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
}
