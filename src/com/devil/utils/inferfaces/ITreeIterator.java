package com.devil.utils.inferfaces;

import java.util.List;

/**
 * 此接口与quickIterateTree配合使用，用来前序遍历一个树型结构 
 */
public interface ITreeIterator<T> {
	/** 根据t返回t的所有顺序子节点 */
	public List<T> getChildren(final T t);

	public void visit(final T t);
}
