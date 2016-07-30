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
			//���ı��仯��ʱ�����
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String number = s.toString();
				String address = AddressDao.find(number);
				if(address!=null){
					tv_address.setText("�����أ�"+address);
				}
			}
			//�ı��仯ǰ����
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			//�ı��仯����õķ���
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
			Toast.makeText(this, "�������ѯ�ĺ���", 0).show();
			return;
		}
		String address = AddressDao.find(number);
		if (TextUtils.isEmpty(address)) {
			tv_address.setText("��ʱû����¼");
		} else {
			tv_address.setText("�����أ�" + address);
		}

	}
}
