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
				String mode = dao.findMode(sender);// 0ȫ������ 1�绰 2����
				if ("0".equals(mode) || "2".equals(mode)) {
					Log.i(TAG, "�����˺��������ţ����ء���");
					abortBroadcast();
					// �����صĶ��Ÿ���¼������ �������ݿ� ������ŵ�ʱ�䣬���ݣ������ˡ���

				}
				// ���ܶ�������
				if (body.contains("fapiao")) {
					Log.i(TAG, "�����˷�Ʊ���ţ����ء���");
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
			case TelephonyManager.CALL_STATE_RINGING:// ����״̬
				String mode = dao.findMode(incomingNumber);
				if ("1".equals(mode) || "0".equals(mode)) {
					Log.i(TAG, "�������绰���Ҷϵ绰");
					Uri uri = Uri.parse("content://call_log/calls/");
					//ע��һ�����ݹ۲��߹۲���м�¼�ı仯
					getContentResolver().registerContentObserver(uri, true, new ContentObserver(new Handler()) {
						@Override
						public void onChange(boolean selfChange) {
							Log.i(TAG,"�۲��߹۲쵽���ݵı仯�ˡ�");
							getContentResolver().unregisterContentObserver(this);
							deleteCallLog(incomingNumber);
							super.onChange(selfChange);
						}
					});
					endCall();// �Ҷϵ绰�� ���м�¼����û�����̵Ĳ���
					
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
	 * ɾ�����м�¼
	 * 
	 * @param incomingNumber
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver resolver = getContentResolver();
		Uri uri = Uri.parse("content://call_log/calls/");
		resolver.delete(uri, "number=?", new String[] { incomingNumber });
	}

	/**
	 * �Ҷϵ绰
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
