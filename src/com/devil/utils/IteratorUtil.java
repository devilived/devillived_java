package com.devil.utils;

import java.util.List;
import java.util.Stack;

public class IteratorUtil {

	/**
	 * 此接口与quickIterateTree配合使用，用来前序遍历一个树型结构
	 */
	public static interface ITreeIterator<T> {
		/** 根据t返回t的所有顺序子节点 */
		public List<T> getChildren(final T t);

		public void visit(final T t);
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
			if (CommUtil.isEmpty(children)) {
				continue;
			}
			for (int i = children.size() - 1; i > -1; i--) {// 到栈里边需要反过来
				stack.push(children.get(i));
			}
		}
	}
}
