package com.parctice.mybat.service;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.springframework.core.io.Resource;

import com.parctice.mybat.entity.Student;
import com.parctice.mybat.mapxml.Pracmy;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class StudentImpl implements Studenservice {
	private static SqlSessionFactory sessionFactory;
	private static Reader reader;
	
	//加载配置文件
	static {
		try {
			reader = Resources.getResourceAsReader("configuration.xml");
			sessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//调用接口编程
	@Test
	public void select() {
		SqlSession sqlSession = sessionFactory.openSession();
		Pracmy pracmy = sqlSession.getMapper(Pracmy.class);
		
		//查询所有的对象，可以作为Student对象，也可以作为Map对象
		/*
		List<Map<String, Object>> sList = new ArrayList<>();
		sList = pracmy.selectAll();
		for(Map<String, Object> student:sList) {
			System.out.println(student.get("name"));
		}
		*/
		
		//查询所有的名字，可以作为String对象
		/*
		List<String> names = new ArrayList<>();
		names = pracmy.selectAllName();
		for(String n:names) {
			System.out.println(n);
		}
		*/
		
		sqlSession.close();
	}
	
}
