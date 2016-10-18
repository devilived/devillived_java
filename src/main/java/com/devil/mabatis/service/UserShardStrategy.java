package com.devil.mabatis.service;

import com.devil.mabatis.service.ShardHelper.ShardStrategy;

public class UserShardStrategy implements ShardStrategy {

	@Override
	public String buildTableNameById(String tableName, long id) {
		return tableName+"_"+(id/2);
	}
	@Override
	public String buildTableNameByIdx(String tableName, int idx) {
		return tableName+"_"+idx;
	}
}
