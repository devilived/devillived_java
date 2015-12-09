package com.devil.mabatis.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devil.mybatis.dao.TestDao;

public class TestService extends AbsService {
	private static final Logger log = LoggerFactory.getLogger(TestService.class);

	public static void main(String[] args) {
		TestService svc = new TestService();
		List<Map> list = svc.findList();
		System.out.println(list);
	}

	public List<Map> findList() {
		SqlSession sess = getSess();
		try {
			TestDao dao = sess.getMapper(TestDao.class);
			List<Map> list = dao.findList();
			return list;
		} finally {
			if (sess != null) {
				sess.close();
			}
		}
	}

}
