package com.devil.mybatis.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

public interface TestDao {

	@Select("SELECT * from test")
	public List<Map> findList();
}
