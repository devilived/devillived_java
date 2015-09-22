package com.devil.mabatis.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

public interface TestDao {

	@Select("	SELECT * from Test")
	public List<Map> findList();
}
