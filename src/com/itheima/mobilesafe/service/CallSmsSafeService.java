package com.itheima.mobilesafe.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.itheima.mobilesafe.db.dao.BlackNumberDao;

public class CallSmsSafeService extends Service {
	public static final String TAG = "CallSmsSafeService";
	private InnerSmsReceiver receiver;
	private BlackNumberDao dao;
	private TelephonyManager tm;
	private MyPhoneListener listener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class InnerSmsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for (Object obj : objs) {
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
				String sender = smsMessage.getOriginatingAddress();
				String body = smsMessage.getMessageBody();
				String mode = dao.findMode(sender);// 0全部拦截 1电话 2短信
				if ("0".equals(mode) || "2".equals(mode)) {
					Log.i(TAG, "发现了黑名单短信，拦截。。");
					abortBroadcast();
					// 把拦截的短信给记录下来。 创建数据库 保存短信的时间，内容，发件人。、

				}
				// 智能短信拦截
				if (body.contains("fapiao")) {
					Log.i(TAG, "发现了发票短信，拦截。。");
					abortBroadcast();
				}
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyPhoneListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		dao = new BlackNumberDao(this);
		receiver = new InnerSmsReceiver();
		IntentFilter filter = new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(receiver, filter);
	}

	private class MyPhoneListener extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, final String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:// 响铃状态
				String mode = dao.findMode(incomingNumber);
				if ("1".equals(mode) || "0".equals(mode)) {
					Log.i(TAG, "黑名单电话，挂断电话");
					Uri uri = Uri.parse("content://call_log/calls/");
					//注册一个内容观察者观察呼叫记录的变化
					getContentResolver().registerContentObserver(uri, true, new ContentObserver(new Handler()) {
						@Override
						public void onChange(boolean selfChange) {
							Log.i(TAG,"观察者观察到数据的变化了。");
							getContentResolver().unregisterContentObserver(this);
							deleteCallLog(incomingNumber);
							super.onChange(selfChange);
						}
					});
					endCall();// 挂断电话后 呼叫记录可能没有立刻的产生
					
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}

	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		receiver = null;
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
		super.onDestroy();
	}

	/**
	 * 删除呼叫记录
	 * 
	 * @param incomingNumber
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver resolver = getContentResolver();
		Uri uri = Uri.parse("content://call_log/calls/");
		resolver.delete(uri, "number=?", new String[] { incomingNumber });
	}

	/**
	 * 挂断电话
	 */
	public void endCall() {
		try {
			Class clazz = CallSmsSafeService.class.getClassLoader().loadClass(
					"android.os.ServiceManager");
			Method method = clazz.getMethod("getService", String.class);
			ITelephony iTelephony = ITelephony.Stub
					.asInterface((IBinder) method.invoke(null,
							Context.TELEPHONY_SERVICE));
			iTelephony.endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
