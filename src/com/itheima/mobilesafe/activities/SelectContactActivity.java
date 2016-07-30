package com.itheima.mobilesafe.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.domain.ContactInfo;
import com.itheima.mobilesafe.engine.ContactInfoProvider;

public class SelectContactActivity extends Activity {
	private List<ContactInfo>  infos;
	private ListView lv_contacts;
	private LinearLayout ll_loading;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_contact);
		lv_contacts = (ListView) findViewById(R.id.lv_contacts);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				//获取所有的系统的联系人信息
				infos = ContactInfoProvider.getContactInfos(SelectContactActivity.this);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ll_loading.setVisibility(View.INVISIBLE);
						lv_contacts.setAdapter(new ContactAdapter());
					}
				});
			};
		}.start();
		
		
		lv_contacts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String phone = infos.get(position).getPhone();
				Intent data = new Intent();
				data.putExtra("phone", phone);
				setResult(0, data);//设置了一个结果数据。
				finish();//关闭掉当前的activity 把数据返回给调用者activity
			}
		});
	}
	
	private class ContactAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return infos.size();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(getApplicationContext(), R.layout.item_contact, null);
			TextView tv_name = (TextView) view.findViewById(R.id.tv_contact_name);
			TextView tv_phone = (TextView)view.findViewById(R.id.tv_contact_phone);
			tv_name.setText(infos.get(position).getName());
			tv_phone.setText(infos.get(position).getPhone());
			return view;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}
	
}
