package com.devil.mabatis.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devil.mybatis.dao.TestDao;
import com.github.pagehelper.PageHelper;

public class TestService extends AbsService {
	private static final Logger log = LoggerFactory.getLogger(TestService.class);

	public static void main(String[] args) {
		TestService svc = new TestService();
		System.out.println(svc.max());
	}
	public Long max() {
		SqlSession sess = getSess();
		try {
			TestDao dao = sess.getMapper(TestDao.class);
			return dao.max();
		} finally {
			if (sess != null) {
				sess.commit();
				sess.close();
			}
		}
	}
	public long count() {
		SqlSession sess = getSess();
		try {
			TestDao dao = sess.getMapper(TestDao.class);
			return dao.count();
		} finally {
			if (sess != null) {
				sess.commit();
				sess.close();
			}
		}
	}
	public List<Map> findList() {
		SqlSession sess = getSess();
		try {
			TestDao dao = sess.getMapper(TestDao.class);
//			dao.insert(0);
//			dao.insert(1);
			dao.insert(2);
			dao.insert(3);
			dao.delete(2);
//			sess.commit();
			List<Map> list = dao.findList(3);
			System.out.println(list);
			return list;
		} finally {
			if (sess != null) {
				sess.commit();
				sess.close();
			}
		}
	}

}
