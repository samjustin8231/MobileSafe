package com.itheima.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.itheima.mobilesafe.R;

public class SmsReceiver extends BroadcastReceiver {
	
	private LocationManager lm;
	private MyListener listener;
	private static final String TAG = "SmsReceiver";
	private DevicePolicyManager dpm;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"有短信到来了");
		//判断是否开启了手机防盗。
		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		boolean protectingstatus = sp.getBoolean("protectingstatus", false);
		if(protectingstatus){
			Log.i(TAG,"手机防盗状态开始的，解析特殊的短信指令");
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			for(Object obj :objs){
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
				String sender = smsMessage.getOriginatingAddress();
				String body = smsMessage.getMessageBody();
				if("#*location*#".equals(body)){
					Log.i(TAG,"获取手机的位置，并且返回给安全号码");
					abortBroadcast();//终止广播，不让小偷看到短信
					//获取手机的位置，并且返回。
					lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
					listener = new MyListener(sp.getString("safenumber", ""));
					lm.requestLocationUpdates("gps", 0, 0, listener);
					
					
				}else if("#*alarm*#".equals(body)){
					Log.i(TAG,"播放报警音乐");
					MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
					player.setLooping(false);
					player.setVolume(1.0f, 1.0f);
					player.start();
					abortBroadcast();
				}else if("#*wipedata*#".equals(body)){
					Log.i(TAG,"远程清除数据。");
					dpm.wipeData(0);
					abortBroadcast();
				}else if("#*lockscreen*#".equals(body)){
					Log.i(TAG,"远程锁屏。");
					dpm.resetPassword("123", 0);
					dpm.lockNow();
					abortBroadcast();
				}
			}
		}
	}
	
	
	private class MyListener implements LocationListener{
		
		private String safenumber;
		public MyListener(String safenumber) {
			this.safenumber = safenumber;
		}

		//当位置变化的时候调用的方法
		@Override
		public void onLocationChanged(Location location) {
			String latitude = "latitude:"+location.getLatitude();
			String longitude  = "longitude:"+location.getLongitude();
			String accuarcy = "accuracy:"+location.getAccuracy();
			SmsManager smsManager = SmsManager.getDefault();
			
			smsManager.sendTextMessage(safenumber, null, latitude+"-"+longitude+"-"+accuarcy, null, null);
			lm.removeUpdates(listener);
			listener = null;
		}

		//当状态变化的时候调用的方法。可用--》不可以  不可以--》可用
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
		//当一个位置提供者可用的时候
		@Override
		public void onProviderEnabled(String provider) {
			
		}
		//当一个位置提供者不可用的时候
		@Override
		public void onProviderDisabled(String provider) {
			
		}
		
	}
}
