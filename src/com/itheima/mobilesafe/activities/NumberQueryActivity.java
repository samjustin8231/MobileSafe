package com.itheima.mobilesafe.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.db.dao.AddressDao;

public class NumberQueryActivity extends Activity {

	private Vibrator mVibrator;
	private EditText et_phone_number;
	private TextView tv_address;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_query);
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		et_phone_number = (EditText) findViewById(R.id.et_phone_number);
		
		et_phone_number.addTextChangedListener(new TextWatcher() {
			//当文本变化的时候调用
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String number = s.toString();
				String address = AddressDao.find(number);
				if(address!=null){
					tv_address.setText("归属地："+address);
				}
			}
			//文本变化前调用
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			//文本变化后调用的方法
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		tv_address = (TextView) findViewById(R.id.tv_address);
	}

	public void query(View view) {
		String number = et_phone_number.getText().toString().trim();
		if (TextUtils.isEmpty(number)) {
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			et_phone_number.startAnimation(shake);
			mVibrator.vibrate(50);
//			mVibrator.vibrate(new long[]{50,20,50,10}, 2);
			Toast.makeText(this, "请输入查询的号码", 0).show();
			return;
		}
		String address = AddressDao.find(number);
		if (TextUtils.isEmpty(address)) {
			tv_address.setText("暂时没有收录");
		} else {
			tv_address.setText("归属地：" + address);
		}

	}
}
