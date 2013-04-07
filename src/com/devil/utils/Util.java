package com.devil.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import com.devil.utils.inferfaces.ITreeIterator;

public final class Util {
	/** 主要用来判断常用的容器字符串是否为空 **/
	public static boolean isNull(Object o) {
		if (o == null) {
			return true;
		}
		if (o instanceof String) {
			return ((String) o).trim().length() == 0;
		} else if (o instanceof Collection) {
			return ((Collection<?>) o).size() == 0;
		} else if (o instanceof Map) {
			return ((Map<?, ?>) o).size() == 0;
		} else if (o.getClass().isArray()) {
			return Array.getLength(o) == 0;
		}
		return false;
	}
	
	/**产生一个固定长度的随机数	 */
	public static int randNumber(int len) {
		int highestUnit = (int) Math.pow(10, len - 1);
		Random random = new Random(47);
		int highestBit = random.nextInt(9) + 1;
		int laterNum = (int) Math.floor(random.nextInt(highestUnit));
		return highestBit * highestUnit + laterNum;
	}

	/** 此方法其实就是迭代非递归前序遍历二叉树的扩展，可以遍历多叉树 */
	public static <T> void quickPreIterateTree(final T root, final ITreeIterator<T> it) {
		Stack<T> stack = new Stack<T>();// 用栈的原因是提高效率
		T p = root;
		if (p == null) {
			return;
		}
		stack.push(p);
		while (stack.size() != 0) {
			p = stack.pop();
			it.visit(p);
			List<T> children = it.getChildren(p);// 得到的list是顺序的
			if (isNull(children)) {
				continue;
			}
			for (int i = children.size() - 1; i > -1; i--) {// 到栈里边需要反过来
				stack.push(children.get(i));
			}
		}
	}
	
	public static String formatException(Throwable e) {
		Writer sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		Throwable cause = e.getCause();
		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		String result = sw.toString();
		pw.close();
		return result;
	}


	public static String readerFromInputStream(InputStream is, String charset)
			throws IOException {
		if (is == null) {
			return null;
		}
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			if (charset == null) {
				isr = new InputStreamReader(is);
			} else {
				isr = new InputStreamReader(is, charset);
			}

			br = new BufferedReader(isr);

			String tmp = null;
			while ((tmp = br.readLine()) != null) {
				sb.append(tmp);
			}
		} finally {
			DebugUtil.close(br);
		}
		return sb.toString();
	}

	public static String readerFromReader(Reader reader) throws IOException {
		if (reader == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(reader);
		String tmp = null;
		while ((tmp = br.readLine()) != null) {
			sb.append(tmp);
		}
		return sb.toString();
	}
}
