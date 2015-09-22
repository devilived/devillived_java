package com.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 最简使用方式如下：
 * Table table1=new Table("table1", new String[]{"t1c1","t1c2","t1c3"});
 * SQLTool sqlTool=new SQLTool(Table [] tableArr）；
 * sqlTool.getSql()
 * 
 * 最完整方式如下：
 * Table table1=new Table("table1", new String[]{"t1c1","t1c2","t1c3"});
 * table1.addFunction(colIdx,IntConst.COUNT);//对某一列进行函数count等操作，可省略；
 * 
 * SQLTool sqlTool=new SQLTool(Table [] tableArr,int centerTableIdx);//第二个参数为中心表的序号，默认第一个表，可空缺
 * 中心表指的是from后边第一个表，一般由这个表链接其他表
 * JoinOnNode joinOn2=new JoinOnNode(table2,"t2c2",table3,"t2c3",JoinOnNode.LEFTJOIN);//一个join on之后的语句，链接的两个表和链接的字段,最后一个表示该链接是左连接还是右链接，可省略，默认左连接
 * 也可用另一种方法简易方法初始化初始化,如下所示
 * JoinOnNode joinOn2=new JoinOnNode(table2,"t1.aaa=t2.bbb",JoinOnNode.LEFTJOIN)//最后一个参数可省，默认左连接
 * sqlTool.setLeftJoins(new JoinOnNode[]{joinOn2});//可添加多次，也可省略
 * 
 * WhereNode wn=new WhereNode(JoinOnNode joinOn,Table table,String conditionStr);//一个条件，前者指添加条件时需要额外添加的链接，可空缺，第二个参数为了区分列是来自哪个表，可空缺。最后是条件，必须填写
 * sqlTool.addWhereNode(wn);//可以添加多次，也可省略
 * 
 * 
 * sqlTool.setGroupBy(table,groupBy);//可省略
 * sqlTool.setGroupBy(table,groupBy);//可省略
 * sqlTool.setLimit(int offset,int lengtth);//可省略,offset可省略
 * 
 * sqlTool.getSql();//得到sql字符串
 * 
 */
public class SQLTool {
	private Table[] tableArr=null;
	private Table centerTable=null;
	private List<JoinOnNode> leftJoins=new ArrayList<JoinOnNode>();
	private List<JoinOnNode> rightJoins=new ArrayList<JoinOnNode>();
	private List<JoinOnNode> innerJoins=new ArrayList<JoinOnNode>();
	private List<WhereNode> whereConditions=new ArrayList<WhereNode>();
	
	private String selectStr="";
	private String fromStr="";
	private String whereStr="";
	private String groupByStr="";
	private String orderByStr="";
	private String topStr="";
	private String limitStr="";
	
	/**************************************************/
	public SQLTool(Table [] tableArr){//默认第0列为中心列
		this(tableArr,0);
	}
	public SQLTool(Table [] tableArr,int centerTableIdx){
		this.tableArr=(tableArr==null?new Table[0]:tableArr);
		this.centerTable=tableArr[(centerTableIdx>=0&&centerTableIdx<tableArr.length?centerTableIdx:0)];
	}
	//本次最终结果
	public String getSql(){
		this.selectStr=this.buildSelectedCols();
		this.whereStr=this.buildWhereNode();//需要在from之前
		this.fromStr=this.buildFrom();
		
		if(this.selectStr==null||"".equals(this.selectStr)){
			this.selectStr="*";
		}
		
		StringBuffer sqlSb=new StringBuffer();
		
		sqlSb.append(SqlKey.SELECT+" ");//"select "
		
		sqlSb.append(this.selectStr+"\n"+SqlKey.FROM+" "+this.fromStr+"\n");//x,x from xx
		
		if(this.whereStr!=null&&this.whereStr.length()>0){
			sqlSb.append(SqlKey.WHERE+" "+this.whereStr+"\n");//where xx
		}
		
		if(this.groupByStr!=null&&this.groupByStr.length()>0){
			sqlSb.append(SqlKey.GROUP_BY+" "+this.groupByStr+"\n");//group by xx
		}
		
		if(this.orderByStr!=null&&this.orderByStr.length()>0){
			sqlSb.append(SqlKey.ORDER_BY+" "+this.orderByStr+"\n");//order by xx
		}
		
		if(this.topStr!=null&&this.topStr.length()>0){
			sqlSb.append(SqlKey.LIMIT+" "+this.limitStr);
		}
		
		//为了保证移植，没有调试用log
		System.out.println(".............................................\n");
		System.out.println(sqlSb.toString()+"\n");
		System.out.println(".............................................\n");
		return sqlSb.toString();
	}
	
	public void setGroupBy(Table table,String groupByCol){
		this.groupByStr=table.getName()+"."+groupByCol;
	}
	
	public void setOrderBy(Table table,String orderByCol){
		this.groupByStr=table.getName()+"."+orderByCol;
	}
	
	public void setLimit(int length){
		this.limitStr=""+length;
	}
	
	public void setLimit(int offset,int length){
		this.limitStr=offset+", "+length;
	}
	
	public void addWhereNode(WhereNode whereNode){//增加一个判断条件
		this.whereConditions.add(whereNode);
	}
	
	public void setLeftJoins(JoinOnNode[] leftJoinTables){
		if(leftJoinTables!=null){
			this.leftJoins.addAll(Arrays.asList(leftJoinTables));
		}
	}
	
	public void setRightJoins(JoinOnNode[] rightJoinTables){
		if(rightJoinTables!=null){
			this.rightJoins.addAll(Arrays.asList(rightJoinTables));
		}
	}
	
	public void setInnerJoins(JoinOnNode[] innerJoinTables){
		if(innerJoinTables!=null){
			this.innerJoins.addAll(Arrays.asList(innerJoinTables));
		}
	}
	
/******************** private functions **************************/
	
	private String buildSelectedCols(){
		StringBuffer colsSb=new StringBuffer();
		for(Table table:tableArr){
			for(int i=0;i<table.getSelectedRows().length;i++)
				colsSb.append(table.getCol(i)+",");
		}
		
		if(','==colsSb.charAt(colsSb.length()-1))
			colsSb.deleteCharAt(colsSb.length()-1);
		
		return colsSb.toString();
	}
	
	private String buildFrom(){
		StringBuffer fromSb=new StringBuffer();
		
		fromSb.append(this.centerTable.getName()+" ");
		for(JoinOnNode JoinOnNode:this.leftJoins){
			fromSb.append(JoinOnNode.getJoinOnNodeString());
		}
		
		for(JoinOnNode JoinOnNode:this.rightJoins){
			fromSb.append(JoinOnNode.getJoinOnNodeString());
		}
		
		for(JoinOnNode JoinOnNode:this.innerJoins){
			fromSb.append(JoinOnNode.getJoinOnNodeString());
		}
		
		return fromSb.toString();
	}
	
	private String buildWhereNode(){
		String retStr="";
		List<JoinOnNode> joinsList=new ArrayList<JoinOnNode>(this.leftJoins.size()+this.rightJoins.size()+this.innerJoins.size());
		joinsList.addAll(this.leftJoins);
		joinsList.addAll(this.rightJoins);
		joinsList.addAll(this.innerJoins);
		
		for(WhereNode whereNode:this.whereConditions){
			if(whereNode.getJoinOn()!=null){
				if(whereNode.getJoinOn().isNeedAppend(joinsList)){
					whereNode.getJoinOn().appendOnCondition(whereNode.getJoinOn());
				}else{
					switch(whereNode.getJoinOn().getJoinType()){
						case JoinOnNode.LEFTJOIN:
							this.leftJoins.add(whereNode.getJoinOn());
							break;
						case JoinOnNode.RIGHTJOIN:
							this.rightJoins.add(whereNode.getJoinOn());
							break;
						case JoinOnNode.INNERJOIN:
							this.innerJoins.add(whereNode.getJoinOn());
							break;
					}
				}
			}
			retStr+=whereNode.getConditionStr()+" "+SqlKey.AND+" ";
		}
		int andIdx=retStr.length()-4;
		if(SqlKey.AND.equals(retStr.substring(andIdx,andIdx+3))){
			retStr=retStr.substring(0,andIdx);
		}
			
		return retStr;
	}
}
