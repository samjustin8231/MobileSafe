package com.itheima.mobilesafe.service;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.db.dao.AddressDao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;



/**
 * ���Ҫ������ ������Ӧ��������¼��Ĵ��壬��Ҫ��3�����顣
 * 1.params.type ����Ϊ������Ӧ�����¼�
 * 2.�޸Ĵ�������  �绰���ȼ���������TYPE_PRIORITY_PHONE
 * 3.����Ȩ��  android.permission.SYSTEM_ALERT_WINDOW
 * @author Administrator
 *
 */
public class CallAddressService extends Service {
	protected static final String TAG = "CallAddressService";
	/**
	 * ����һ���绰״̬�Ĺ�����
	 */
	private TelephonyManager tm;
	/**
	 * �绰״̬�ļ�����
	 */
	private MyPhoneListener listener;

	private OutCallInnerReceiver receiver;

	/**
	 * ϵͳ����Ĺ�����
	 */
	private WindowManager mWm;

	/**
	 * ��ĳ�Ա���� ��ʾ��������˾��view����
	 */
	private View view;

	private WindowManager.LayoutParams params;
	
	private SharedPreferences sp;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * �ڷ�����ڲ�������һ���㲥�����ߣ�ϣ���㲥�����ߵĴ�����ڸ�����һ�¡�
	 * 
	 * @author Administrator
	 * 
	 */
	private class OutCallInnerReceiver extends BroadcastReceiver {
		private static final String TAG = "CallAddressService";

		@Override
		public void onReceive(Context context, Intent intent) {
			String number = getResultData();
			Log.i(TAG, "���µĵ绰���ȥ�ˣ������ǣ�" + number);
			String address = AddressDao.find(number);
			if (!TextUtils.isEmpty(address)) {
				// Toast.makeText(getApplicationContext(), address, 1).show();
				showMyToast(address);
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sp = getSharedPreferences("config", MODE_PRIVATE);
		// ��ʼ�����������
		mWm = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		receiver = new OutCallInnerReceiver();
		// ���˵绰���ȥ�Ķ���
		IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(receiver, filter);

		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyPhoneListener();
		// �ü����������绰����״̬�ı仯
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	long[] mHits = new long[2];
	/**
	 * ��ʾ�Զ������˾
	 * 
	 * @param address
	 *            �绰����Ĺ�����
	 */
	public void showMyToast(String address) {
		view = View.inflate(this, R.layout.toast_address, null);
		view.setBackgroundResource(R.drawable.call_locate_orange);
		//��view����ע��һ������¼�
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);//����������λ����
		           mHits[mHits.length-1] = SystemClock.uptimeMillis();
		           if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
		        	   params.x = mWm.getDefaultDisplay().getWidth()/2 - view.getWidth()/2;
		        	   mWm.updateViewLayout(view, params);
		        	   //��¼��λ��
		        	   Editor editor = sp.edit();
						editor.putInt("paramsx", params.x);
						editor.commit();
		           }
			}
		});
		//��view������ɴ����ļ�����
		view.setOnTouchListener(new OnTouchListener() {
			//��ָ����Ļ�ϵ�һ�ΰ��µĳ�ʼ����
			int startX ;
			int startY ;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN://��ָ������Ļ ��һ�νӴ���Ļ
					Log.i(TAG,"��ָ������Ļ");
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					Log.i(TAG,"startX �� "+startX);
					Log.i(TAG,"startY �� "+startY);
					break;

				case MotionEvent.ACTION_MOVE: //��ָ����Ļ�ϴ����ƶ�
					Log.i(TAG,"��ָ����Ļ���ƶ�");
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					Log.i(TAG,"newX �� "+newX);
					Log.i(TAG,"newY �� "+newY);
					int dx = newX - startX;
					int dy = newY - startY;
					Log.i(TAG,"��ָˮƽ����ƫ����dx �� "+dx);
					Log.i(TAG,"��ָ��ֱ����ƫ����dy �� "+dy);
					//�����ÿؼ�Ҳ��������ָ�ƶ� dx dy��
					params.x+=dx;
					params.y+=dy;
					//�߽��ж�
					if(params.x<0){//�����Ƴ���Ļ
						params.x =0;
					}
					if(params.y<0){//�����Ƴ���Ļ
						params.y = 0;
					}
					if(params.x> (mWm.getDefaultDisplay().getWidth()-view.getWidth())){//�����Ƴ���Ļ
						params.x = mWm.getDefaultDisplay().getWidth()-view.getWidth();
					}
					if(params.y> (mWm.getDefaultDisplay().getHeight()-view.getHeight())){//�����Ƴ���Ļ
						params.y = mWm.getDefaultDisplay().getHeight()-view.getHeight();
					}
					
					mWm.updateViewLayout(view, params);
					//�ظ���һ���Ĳ��� �����³�ʼ����ָ�Ŀ�ʼλ�á�
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
					
				case MotionEvent.ACTION_UP://��ָ�뿪��Ļһ˲���Ӧ�Ķ���
					Log.i(TAG,"��ָ�뿪��Ļ");//��ָ�뿪��Ļ��һ˲�� ��¼�ؼ�����Ļ�ϵ�λ��
					Editor editor = sp.edit();
					editor.putInt("paramsx", params.x);
					editor.putInt("paramsy", params.y);
					editor.commit();
					break;
				}
				return false;//True if the listener has consumed the event, false otherwise.
				              //true ��������� �����������¼���false������û�д�������¼���
			}
		});

		
		TextView tv_address = (TextView) view.findViewById(R.id.tv_address);
		tv_address.setText(address);
		//��˾��ʾ�Ĳ���
		params = new WindowManager.LayoutParams();
		//���䷽ʽ
		params.gravity = Gravity.LEFT+Gravity.TOP;
		
		//ָ��������Ļ��ߵľ��� ������ Gravity.LEFTͬʱʹ��
		params.x = sp.getInt("paramsx", 0);
		//ָ��������Ļ�ϱߵľ��� ������ Gravity.TOPͬʱʹ��
		params.y = sp.getInt("paramsy", 0);
		
		// ��˾�Ŀ��
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		// ��˾�Ĳ��� ���ɻ�ȡ���� �����Ա��� ������Ļ����
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				//| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		// ��͸������
		params.format = PixelFormat.TRANSLUCENT;
		params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;//���õ绰���ȼ��Ĵ������ͣ��������Ϳ�����Ӧ�����¼���
		mWm.addView(view, params);
	}

	private class MyPhoneListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:// ����״̬��
				if (view != null) {
					mWm.removeView(view);
					view = null;
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING:// ���� ����״̬ ���˸����绰
				String address = AddressDao.find(incomingNumber);
				if (!TextUtils.isEmpty(address)) {
					// Toast.makeText(getApplicationContext(), address,
					// 1).show();
					showMyToast(address);
				}
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:// ��ͨ�绰��״̬ ͨ��״̬��

				break;
			}
		}

	}

	// ����ֹͣ��ʱ����õķ���
	@Override
	public void onDestroy() {
		// ȡ���绰״̬�ļ���
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
		// ����ֹͣȡ��ע��㲥������
		unregisterReceiver(receiver);
		receiver = null;

		super.onDestroy();
	}

}
