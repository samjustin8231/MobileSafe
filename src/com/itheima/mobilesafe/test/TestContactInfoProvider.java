package com.itheima.mobilesafe.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.itheima.mobilesafe.domain.ContactInfo;
import com.itheima.mobilesafe.engine.ContactInfoProvider;

public class TestContactInfoProvider extends AndroidTestCase {
	public void test() throws Exception{
		List<ContactInfo> infos = ContactInfoProvider.getContactInfos(getContext());
		for(ContactInfo info: infos){
			System.out.println(info.toString());
		}
	}
}
