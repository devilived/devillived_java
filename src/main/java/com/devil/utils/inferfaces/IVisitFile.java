package com.devil.utils.inferfaces;

import java.io.File;

/**
 * <p>遍历到一个文件节点是要做的事情</p>
 * <p>what to to when visit a file node</p>
 * @author devil
 *
 */
public interface IVisitFile {
	public void visit(File f);
}
