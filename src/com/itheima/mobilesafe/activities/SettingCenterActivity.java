package com.itheima.mobilesafe.activities;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.service.CallAddressService;
import com.itheima.mobilesafe.service.CallSmsSafeService;
import com.itheima.mobilesafe.ui.SettingView;
import com.itheima.mobilesafe.utils.ServiceStatusUtils;

public class SettingCenterActivity extends Activity implements OnClickListener {
	
	
	//声明归属地显示的控件
	private SettingView sv_show_address;
	private Intent showAddressIntent;
	private SharedPreferences sp;
	
	//声明自动更新控件
	private SettingView sv_auto_update;
	
	
	//声明黑名单拦截设置控件
	private SettingView sv_callsms_safe;
	private Intent callSmsSafeIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_center);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		//归属地显示的初始化操作
		showAddressIntent = new Intent(this, CallAddressService.class);
		sv_show_address = (SettingView) findViewById(R.id.sv_show_address);
		sv_show_address.setOnClickListener(this);

		//自动更新初始化操作
		sv_auto_update = (SettingView) findViewById(R.id.sv_auto_update);
		sv_auto_update.setOnClickListener(this);
		
		//黑名单拦截初始化
		sv_callsms_safe = (SettingView) findViewById(R.id.sv_callsms_safe);
		sv_callsms_safe.setOnClickListener(this);
		callSmsSafeIntent = new Intent(this,CallSmsSafeService.class);
		
	}

	/**
	 * 当用户看到activity界面的时候调用的方法
	 */
	@Override
	protected void onStart() {
		// 不应该记录服务的状态，应该是动态的去检查 检查服务的状态
		boolean showaddress = ServiceStatusUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.CallAddressService");
		sv_show_address.setChecked(showaddress);
		boolean autoupdate = sp.getBoolean("autoupdate", false);
		sv_auto_update.setChecked(autoupdate);
		
		boolean callsmssafe = ServiceStatusUtils.isServiceRunning(this,
				"com.itheima.mobilesafe.service.CallSmsSafeService");
		sv_callsms_safe.setChecked(callsmssafe);
		
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sv_show_address:
			//判断组合控件是否被点击。
			if(sv_show_address.isChecked()){
				sv_show_address.setChecked(false);
				stopService(showAddressIntent);
			}else{
				sv_show_address.setChecked(true);
				startService(showAddressIntent);
			}
			break;
		case R.id.sv_auto_update:
			Editor editor = sp.edit();
			if(sv_auto_update.isChecked()){
				sv_auto_update.setChecked(false);
				editor.putBoolean("autoupdate", false);
			}else{
				sv_auto_update.setChecked(true);
				editor.putBoolean("autoupdate", true);
			}
			editor.commit();
			break;
		case R.id.sv_callsms_safe:
			//判断组合控件是否被点击。
			if(sv_callsms_safe.isChecked()){
				sv_callsms_safe.setChecked(false);
				stopService(callSmsSafeIntent);
			}else{
				sv_callsms_safe.setChecked(true);
				startService(callSmsSafeIntent);
			}
			break;
		}
	}
}
