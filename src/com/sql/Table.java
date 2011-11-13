package com.sql;

import java.util.HashMap;
import java.util.Map;

public class Table{
	private String name;
	private String alias;
	private String[] selectedRows;
	
	private Map<Integer,Integer> funcColMap=new HashMap<Integer,Integer>();
	
	public Table(String tableName, String[] selectedRows) {
		this.name = tableName;
		this.selectedRows = (selectedRows==null?new String[0]:selectedRows);
	}
	public void setAlias(String alias){
		this.alias=alias;
	}
	
	
	public void addFunction(int colIdx,int funcType){
		this.funcColMap.put(colIdx, funcType);
	}
/************************************************************************/	
	protected String getJoinName(){//在join节点中使用
		if(this.alias==null||"".equals(this.alias)){
			return this.name;
		}else
			return this.name+" as "+this.alias;
	}
	protected String getName(){//得到表名
		if(this.alias==null||"".equals(this.alias)){
			return this.name;
		}else
			return this.alias;
	}
	protected String getCol(int colIdx){//得到alias.col形式的列名
		String retStr="";
		
		if(colIdx>=0&&colIdx<this.selectedRows.length){
			if(this.alias!=null&&this.alias.length()>0)
				retStr = this.alias+"."+this.selectedRows[colIdx];
			else
				retStr = this.name+"."+this.selectedRows[colIdx];
			
			Integer funcType=this.funcColMap.get(colIdx);
			if(funcType!=null){
				switch(funcType){
					case IntConst.AVG:
						retStr=SqlKey.AVG+"( "+retStr+" )";
						break;
					case IntConst.COUNT:
						retStr=SqlKey.COUNT+"( "+retStr+" )";
						break;
					case IntConst.FIRST:
						retStr=SqlKey.FIRST+"( "+retStr+" )";
						break;
					case IntConst.LAST:
						retStr=SqlKey.LAST+"( "+retStr+" )";
						break;
					case IntConst.MAX:
						retStr=SqlKey.MAX+"( "+retStr+" )";
						break;
					case IntConst.MIN:
						retStr=SqlKey.MIN+"( "+retStr+" )";
						break;
					case IntConst.SUM:
						retStr=SqlKey.SUM+"( "+retStr+" )";
						break;
					default:
						retStr="";
							
				}
			}
		}else 
			retStr="";
		
		return retStr;
	}
	
	protected String[] getSelectedRows(){
		return this.selectedRows;
	}
}
