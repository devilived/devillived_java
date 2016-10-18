package com.devil.mabatis.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devil.utils.ClzUtil;
import com.devil.utils.CommUtil;

@Intercepts({
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
				RowBounds.class, ResultHandler.class }),
		@Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })

public class ShardHelper implements Interceptor {
	private static final Logger log = LoggerFactory.getLogger(ShardHelper.class);
	private ShardStrategy strategy;
	private Map<Class<?>, String> includeMap = new HashMap<>();
	private Set<String> includeSet = new HashSet<>();

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		// 获取原始的ms
		Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		String msid = ms.getId();
		int lastDotIdx = msid.lastIndexOf(".");
		String className = msid.substring(0, lastDotIdx);
		boolean valid = false;
		for (String str : includeSet) {
			if (msid.startsWith(str)) {
				valid = true;
				break;
			}
		}
		if (!valid) {
			return invocation.proceed();
		}
		String methodName = msid.substring(lastDotIdx + 1);
		Class<?> daoClass = Class.forName(className);
		Method method = ClzUtil.getMethod(daoClass, methodName);
		Shard shard = method.getAnnotation(Shard.class);
		if (shard != null) {
			ShardType type = shard.value();
			SqlSource sqlSource = ms.getSqlSource();
			try {
				String tableName = includeMap.get(daoClass); 
				if (type == ShardType.OneById) {
					Object paramobj = args[1];

					long id;
					if (paramobj instanceof ParamMap) {
						Object value = OgnlCache.getValue(shard.idKey(), paramobj);
						id=Long.valueOf(value.toString());
					} else {
						id = (long) paramobj;
					}
					
					String newTableName = strategy.buildTableNameById(tableName, id);

					ShardSqlSource shardSqlSource = new ShardSqlSource(sqlSource, tableName, newTableName);
					ClzUtil.setField(ms, "sqlSource", shardSqlSource);
					Object result=null;
					try{
					result = invocation.proceed();
					} catch (InvocationTargetException e) {
						Throwable newe = CommUtil.getException(e, "com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException");
						if (newe != null) {
							if(ms.getSqlCommandType()==SqlCommandType.INSERT){
								StaticSqlSource createSqlSource = new StaticSqlSource(ms.getConfiguration(), "CREATE TABLE "+newTableName+" LIKE "+tableName);
								MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "_CREATE", createSqlSource, SqlCommandType.UPDATE);
						        builder.resource(ms.getResource());
						        builder.fetchSize(1);
						        builder.statementType(StatementType.STATEMENT);
						        builder.keyGenerator(ms.getKeyGenerator());
						        builder.timeout(ms.getTimeout());
						        //count查询返回值int
						        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
						        List<ResultMapping> EMPTY_RESULTMAPPING = Collections.emptyList();
						        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), int.class, EMPTY_RESULTMAPPING).build();
						        resultMaps.add(resultMap);
						        builder.resultMaps(resultMaps);
						        builder.resultSetType(ms.getResultSetType());
						        builder.cache(ms.getCache());
						        builder.flushCacheRequired(ms.isFlushCacheRequired());
						        builder.useCache(ms.isUseCache());
						        args[0]=builder.build();
						        Object tmpresult = invocation.proceed();
						        System.err.println(tmpresult);
						        args[0]=ms;
								return invocation.proceed();
							}
						}
						throw e;
					}
					return result;
					
				} else if (type == ShardType.One) {
					int tableid = 0;
					while (true) {
						try {
							String newTableName = strategy.buildTableNameByIdx(includeMap.get(daoClass), tableid);
							ShardSqlSource shardSqlSource = new ShardSqlSource(sqlSource, tableName, newTableName);
							ClzUtil.setField(ms, "sqlSource", shardSqlSource);
							Object obj = invocation.proceed();
							if (obj != null) {
								return obj;
							}
							tableid++;
						} catch (InvocationTargetException e) {
							Throwable newe = CommUtil.getException(e, "com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException");
							if (newe != null) {
								return null;
							}
							throw e;
						}
					}
				} else if (type == ShardType.Count) {
					int tableid = 0;
					long cnt = 0;
					List<Long> resultlist = null;
					while (true) {
						try {
							String newTableName = strategy.buildTableNameByIdx(tableName,tableid);
							ShardSqlSource shardSqlSource = new ShardSqlSource(sqlSource, tableName, newTableName);
							ClzUtil.setField(ms, "sqlSource", shardSqlSource);
							resultlist = (List<Long>) invocation.proceed();
							Long thiscnt = resultlist.get(0);
							cnt += thiscnt;
							tableid++;
						} catch (InvocationTargetException e) {
							Throwable newe = CommUtil.getException(e, "com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException");
							if (newe != null) {
								resultlist.set(0, cnt);
								return resultlist;
							}
							throw e;
						}
					}
				} else if (type == ShardType.ManyById) {
					Object paramobj = args[1];
					Object value = OgnlCache.getValue(shard.idKey(), paramobj);
					Collection<Long> ids = null;
					if (value instanceof Collection) {
						ids = (Collection) value;
					} else if (value.getClass().isArray()) {
						Long[] arr = (Long[]) value;
						ids = Arrays.asList(arr);
					}

					Map<String, List<Long>> uidsegmap = new HashMap<>();
					for (Long uid : ids) {
						String tblName = strategy.buildTableNameById(tableName, uid);
						List<Long> uidseg = uidsegmap.get(tblName);// new
																	// ArrayList<>();
						if (uidseg == null) {
							uidseg = new ArrayList<>();
							uidsegmap.put(tblName, uidseg);
						}
						uidseg.add(uid);
					}
					List<?> resultlist = new ArrayList<>();
					for (Entry<String, List<Long>> entry : uidsegmap.entrySet()) {
						try {
							String newTableName = entry.getKey();

							Map<String, Object> newParam = new ParamMap<Object>();
							newParam.put(shard.idKey(), entry.getValue());

							ShardInSqlSource ShardInSqlSource = new ShardInSqlSource(sqlSource, tableName, newTableName,
									newParam);
							ClzUtil.setField(ms, "sqlSource", ShardInSqlSource);
							resultlist.addAll((Collection) invocation.proceed());
						} catch (Exception e) {
							log.error("shard manybyid error", e);
						}
					}
					return resultlist;
				} else if (type == ShardType.Many) {
					int tableid = 0;
					List<?> resultlist = new ArrayList<>();
					while (true) {
						try {
							String newTableName = strategy.buildTableNameByIdx(tableName,tableid);
							ShardSqlSource shardSqlSource = new ShardSqlSource(sqlSource, tableName, newTableName);
							ClzUtil.setField(ms, "sqlSource", shardSqlSource);
							resultlist.addAll((Collection) invocation.proceed());
							tableid++;
						} catch (InvocationTargetException e) {
							Throwable newe = CommUtil.getException(e, "com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException");
							if (newe != null) {
								return resultlist;
							}
							throw e;
						}
					}
				}else if(type == ShardType.Max||type == ShardType.Min){
					int tableid = 0;
					Set<Long> set=new HashSet<>();
					List<Long> resultlist = null;
					while (true) {
						try {
							String newTableName = strategy.buildTableNameByIdx(tableName,tableid);
							ShardSqlSource shardSqlSource = new ShardSqlSource(sqlSource, tableName, newTableName);
							ClzUtil.setField(ms, "sqlSource", shardSqlSource);
							resultlist = (List<Long>) invocation.proceed();
							if (!resultlist.isEmpty()) {
								set.add(resultlist.get(0));
							}
							tableid++;
						} catch (InvocationTargetException e) {
							Throwable newe = CommUtil.getException(e, "com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException");
							if (newe != null) {
								Long value = (type == ShardType.Max?Collections.max(set):Collections.min(set));
								resultlist.set(0, value);
								return resultlist;
							}
							throw e;
						}
					}
				}
				// 传递给下一个拦截器处理
			} finally {
				ClzUtil.setField(ms, "sqlSource", sqlSource);
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		// 当目标类是StatementHandler类型时，才包装目标类，否者直接返回目标本身,减少目标被代理的
		// 次数
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {
		try {
			String include = properties.getProperty("include");
			String[] items = include.split(",");
			for (String item : items) {
				String[] kv = item.split("=");
				String clzName = kv[0];
				String tableName = kv[1];
				includeSet.add(clzName);
				includeMap.put(Class.forName(clzName), tableName);
			}
			Class<ShardStrategy> strategyClz = (Class<ShardStrategy>) Class.forName(properties.getProperty("strategy"));
			this.strategy = strategyClz.newInstance();
			System.out.println(properties);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Target({ ElementType.FIELD, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public @interface Shard {
		public ShardType value();
		public String idKey() default "";
	}
	public static enum ShardType{One,OneById,Many,ManyById,Count,Max,Min};
	public static interface ShardStrategy {
		public String buildTableNameById(String tableName, long id);

		public String buildTableNameByIdx(String tableName, int tableIdx);
	}
	
	private static class ShardSqlSource implements SqlSource {
		private SqlSource sqlSource;
		private String oldTableName;
		private String newTableName;

		public ShardSqlSource(SqlSource src,String oldTableName,String newTableName) {
			this.sqlSource = src;
			this.oldTableName=oldTableName;
			this.newTableName=newTableName;
		}

		@Override
		public BoundSql getBoundSql(Object parameterObject) {
			BoundSql boundSql =  sqlSource.getBoundSql(parameterObject);
			String sql = boundSql.getSql().replaceAll(oldTableName, newTableName);
			ClzUtil.setField(boundSql, "sql", sql);
			return boundSql;
		}
	}
	private static class ShardInSqlSource implements SqlSource {
		private SqlSource sqlSource;
		private String oldTableName;
		private String newTableName;
		private Object newParameterObject;

		public ShardInSqlSource(SqlSource src, String oldTableName, String newTableName, Object newParameterObject) {
			this.sqlSource = src;
			this.oldTableName = oldTableName;
			this.newTableName = newTableName;
			this.newParameterObject = newParameterObject;
		}

		@Override
		public BoundSql getBoundSql(Object parameterObject) {
			BoundSql boundSql =  sqlSource.getBoundSql(newParameterObject);
			String sql = boundSql.getSql().replaceAll(oldTableName, newTableName);
			ClzUtil.setField(boundSql, "sql", sql);
			return boundSql;
		}
	}
}