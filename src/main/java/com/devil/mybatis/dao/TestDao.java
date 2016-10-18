package com.devil.mybatis.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.devil.mabatis.service.ShardHelper.Shard;
import com.devil.mabatis.service.ShardHelper.ShardType;

public interface TestDao {
	
	@Shard(value =ShardType.OneById,idKey="id")
	@Insert("INSERT INTO test(id,`value`)VALUES(${id},'b') ")
	public int insert(@Param("id") Integer id);

	@Shard(value =ShardType.OneById,idKey="id")
	@Select("SELECT * from test where id=${id} ")
	public List<Map> findList(@Param("id") Integer id);
	
	@Shard(value =ShardType.OneById,idKey="id")
	@Delete("DELETE from test where id=${id}")
	public void delete(@Param("id")Integer id);
	
	@Shard(value =ShardType.Count)
	@Select("SELECT count(0) from test")
	public long count();
	
	@Shard(value =ShardType.Max)
	@Select("SELECT max(id) from test")
	public Long max();
}
