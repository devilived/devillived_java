package com.devil.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
}
