package com.itheima.mobilesafe.test;

import com.itheima.mobilesafe.db.dao.AddressDao;

import android.test.AndroidTestCase;

public class TestAddressDao extends AndroidTestCase {
	public void testGetAddress() throws Exception{
		String address = AddressDao.find("13612345678");
		System.out.println(address);
	}
}
