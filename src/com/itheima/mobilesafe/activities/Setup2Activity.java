package com.itheima.mobilesafe.activities;

import com.itheima.mobilesafe.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Setup2Activity extends BaseSetupActivity {
	private TelephonyManager tm;
	private ImageView iv_setup2_status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup2);
		tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		iv_setup2_status = (ImageView) findViewById(R.id.iv_setup2_status);
		//判断sim卡绑定的状态。 如果sim绑定了修改图标。
		String savedSim = sp.getString("sim", "");
		if (!TextUtils.isEmpty(savedSim)) {
			iv_setup2_status.setImageResource(R.drawable.lock);
		}
		
	}

	public void next(View view) {
		//判断 是否绑定了sim卡 如果没有绑定提示用户绑定。 
		String savedSim = sp.getString("sim", "");
		if (TextUtils.isEmpty(savedSim)) {
			Toast.makeText(this, "请先绑定sim卡", 1).show();
			return;
		}
		
		loadActivity(Setup3Activity.class);
		overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
	}

	public void pre(View view) {
		loadActivity(Setup1Activity.class);
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

	/**
	 * 绑定sim卡对应的点击事件
	 * 
	 * @param view
	 */
	public void bindSim(View view) {
		// 判断是否已经绑定了sim卡。
		String savedSim = sp.getString("sim", "");
		if (TextUtils.isEmpty(savedSim)) {
			String sim = tm.getSimSerialNumber();// 获取sim卡的唯一串号。
			Editor editor = sp.edit();
			editor.putString("sim", sim);
			editor.commit();
			iv_setup2_status.setImageResource(R.drawable.lock);
		}else{//原来绑定过sim卡
			Editor editor = sp.edit();
			editor.putString("sim", null);
			editor.commit();
			iv_setup2_status.setImageResource(R.drawable.unlock);
		}
	}
}
