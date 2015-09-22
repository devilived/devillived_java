package com.sql;

public class WhereNode{
	private JoinOnNode joinOn;
	private String conditionStr;
	private Table table;
	
	public WhereNode(String conditionStr){
		this(null,null,conditionStr);
	}
	
	public WhereNode(JoinOnNode joinOn,String conditionStr){
		this(joinOn, null,conditionStr);
	}
	
	public WhereNode(Table table,String conditionStr){
		this(null, table,conditionStr);
	}
	
	public WhereNode(JoinOnNode joinOn,Table table,String conditionStr){
		this.joinOn=joinOn;
		this.table=table;
		if(this.table!=null)
			this.conditionStr=this.table.getName()+"."+conditionStr;
		else
			this.conditionStr=conditionStr;
	}
	/**************************************************************************/
	protected JoinOnNode getJoinOn() {
		return joinOn;
	}
	protected String getConditionStr() {
		return conditionStr;
	}
}
