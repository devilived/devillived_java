package com.devil.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public final class DebugUtil {
	public static <T extends Comparable<? super T>> List<T> sort(Collection<T> collection) {
		List<T> list = new ArrayList<>(collection);
		Collections.sort(list);
		return list;
	}

	public static <T extends Comparable<? super T>> T[] sort(T... arr) {
		Arrays.sort(arr);
		return arr;
	}

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
}
