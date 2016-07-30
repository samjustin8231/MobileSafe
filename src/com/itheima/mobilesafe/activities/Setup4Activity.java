package com.itheima.mobilesafe.activities;

import com.itheima.mobilesafe.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Setup4Activity extends BaseSetupActivity {
	private CheckBox cb_setup4_status;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup4);
		cb_setup4_status = (CheckBox) findViewById(R.id.cb_setup4_status);
		boolean protectingstatus = sp.getBoolean("protectingstatus", false);
		if(protectingstatus){
			cb_setup4_status.setChecked(true);
			cb_setup4_status.setText("防盗保护已经开启");
		}else{
			cb_setup4_status.setChecked(false);
			cb_setup4_status.setText("防盗保护没有开启");
		}
		
		cb_setup4_status.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					cb_setup4_status.setText("防盗保护已经开启");
				}else{
					cb_setup4_status.setText("防盗保护没有开启");
				}
				Editor editor = sp.edit();
				editor.putBoolean("protectingstatus", isChecked);
				editor.commit();
			}
		});
	}
	
	public void next(View view){
		//在sp里面存放一个finishsetup  ---》true
		Editor editor = sp.edit();
		editor.putBoolean("finishsetup", true);
		editor.commit();
		loadActivity(LostFindActivity.class);
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	}
	
	public void pre(View view){
		loadActivity(Setup3Activity.class);
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
	}

	@Override
	protected void showNext() {
		next(null);
		
	}

	@Override
	protected void showPre() {
		pre(null);
	}
}
