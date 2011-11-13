package com.sql;

import java.util.Collection;

/*
 * 链接的一个节点，包括 left join table2 on table1.leftCol = table2.rightCol and ...
 */
public class JoinOnNode{
	public static final int LEFTJOIN=1;
	public static final int RIGHTJOIN=2;
	public static final int INNERJOIN=3;
	private int joinType=JoinOnNode.LEFTJOIN;//默认左连接
	
	private String leftCol="";//这两个变量只有在new新对象时有意义，如果on后有多个连接条件，则失去意义
	private String rightCol="";
	
	private Table joiningTable;//=号连接左侧表
	private Table joinedTable;//=号右侧表，同时也是被连接的表,即紧跟join on 的表
	private String onStr;
	
	public JoinOnNode(Table table,String onStr){//默认左连接
		this.joinedTable=table;
		this.onStr=onStr;
	}
	public JoinOnNode(Table table,String onStr,int joinType){
		this.joinedTable=table;
		this.onStr=onStr;
		this.joinType=joinType;
	}
	
	public JoinOnNode(Table joiningTable,String leftCol,Table joinedTable,String rightCol){//默认左连接
		this( joiningTable, leftCol, joinedTable, rightCol, JoinOnNode.LEFTJOIN);
	}
	public JoinOnNode(Table joiningTable,String leftCol,Table joinedTable,String rightCol,int joinType){//可以设置连接
		this.joiningTable=joinedTable;
		this.joinedTable=joinedTable;
		this.leftCol=leftCol;
		this.rightCol=rightCol;
		this.joinType=joinType;
		this.onStr=this.joiningTable.getName()+"."+this.leftCol.toLowerCase()+" = "+this.joinedTable.getName()+"."+this.rightCol.toLowerCase()+" ";
	}
	
	/*********************************************************************************/
	/*
	 * 获取结果如下：left join joinedTable on joiningTable.leftCol = joinedTable.rightCol
	 */
	protected String getJoinOnNodeString(){
		String retStr="";
		switch(this.joinType){
			case JoinOnNode.LEFTJOIN:
				retStr=SqlKey.LEFT_JOIN+" ";
				break;
			case JoinOnNode.RIGHTJOIN:
				retStr=SqlKey.RIGHT_JOIN+" ";
				break;
			case JoinOnNode.INNERJOIN:
				retStr=SqlKey.INNER_JOIN+" ";
				break;
			default:
				retStr=SqlKey.LEFT_JOIN+" ";
		}
		
		if(this.joinedTable==null)
			retStr+= "";
		else
			retStr+= this.joinedTable.getJoinName();
		
		return retStr+" "+SqlKey.ON+" "+this.onStr+" ";
	}
	protected boolean isNeedAppend(Collection<JoinOnNode> joinOns){//如果连接类型和表名是相同，则追加，否则，新的创建
		for(JoinOnNode joinOn:joinOns){
			if(joinOn!=null&&joinOn.joiningTable!=null&&joinOn.joinedTable!=null
					&&this.joiningTable!=null&&this.joinedTable!=null){
				if(this.joiningTable.equals(joinOn.joiningTable)&&this.joinedTable.equals(joinOn.joinedTable)&&this.joinType==joinOn.joinType){
					return true;
				};
			}
		}
		return false;
	}
	
	/*
	 * 如果on需要多个条件，则在on条件后追加条件，使用前需要用isNeedAppend判断 是否需要真的追加条件
	 */
	protected void appendOnCondition(JoinOnNode joinOn){
		String leftCol=joinOn.leftCol;
		String rightCol=joinOn.rightCol;
		String noSpaceStr=this.onStr.replaceAll("\\s", "");//为了提高效率，不直接用正则表达式
		if(noSpaceStr.contains(leftCol.toLowerCase()+"="+rightCol.toLowerCase())||noSpaceStr.contains(rightCol.toLowerCase()+"="+leftCol.toLowerCase())){
			return;
		}else{
			this.onStr += " "+SqlKey.AND+" "+leftCol.toLowerCase()+" = "+rightCol.toLowerCase();
		};
	}
	
	protected int getJoinType(){
		return this.joinType;
	}
}
