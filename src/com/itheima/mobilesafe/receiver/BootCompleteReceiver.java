package com.itheima.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

	private static final String TAG = "BootCompleteReceiver";
	private TelephonyManager tm;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"哈哈哈，手机启动了。。。");
		//判断用户是否开启了手机防盗功能。
		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		boolean protectingstatus = sp.getBoolean("protectingstatus", false);
		if(protectingstatus){
			Log.i(TAG,"手机防盗是开启i状态");
			//判断当前手机里面的sim卡 与 我原来绑定的sim卡是否一致。
			tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String currentSim = tm.getSimSerialNumber();//当前手机里面的sim卡串号
			String bindSim = sp.getString("sim", "")+"adfsagb";
			if(bindSim.equals(currentSim)){
				//sim卡没有变化，就是你的卡
			}else{
				//sim卡变化了。。有可能手机被盗，偷偷在后台发送一个报警短信。
				SmsManager smsManager = SmsManager.getDefault();
				String safenumber = sp.getString("safenumber", "");
				smsManager.sendTextMessage(safenumber, null, "sim changed", null, null);
			}
		}else{
			Log.i(TAG,"手机防盗是没有开启的状态");
		}
		
	}

}
