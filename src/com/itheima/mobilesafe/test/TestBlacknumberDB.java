package com.itheima.mobilesafe.test;

import java.util.List;
import java.util.Random;

import android.test.AndroidTestCase;

import com.itheima.mobilesafe.db.BlacknumberDBOpenHelper;
import com.itheima.mobilesafe.db.dao.BlackNumberDao;
import com.itheima.mobilesafe.domain.BlackNumber;

public class TestBlacknumberDB extends AndroidTestCase {
	public void testCreateDB() throws Exception {
		BlacknumberDBOpenHelper helper = new BlacknumberDBOpenHelper(
				getContext());
		helper.getWritableDatabase();
	}

	public void testAdd() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		long basenumber = 13500000000L;
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			dao.add(String.valueOf(basenumber+i), String.valueOf(random.nextInt(3)));
		}
	}

	public void testDelete() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		dao.delete("666");
	}

	public void testUpdate() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		dao.update("666", "1");
	}

	public void testFind() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		String mode = dao.findMode("666");
		if (mode == null) {
			System.out.println("黑名单号码不存在");
		} else {
			System.out.println("拦截模式：" + mode);
		}
	}
	
	public void testFindAll() throws Exception{
		BlackNumberDao dao = new BlackNumberDao(getContext());
		List<BlackNumber> infos = dao.findAll();
		for(BlackNumber info:infos){
			System.out.println(info.toString());
		}
	}
}
