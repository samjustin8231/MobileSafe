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
 * 如果要弹出来 可以相应触摸点击事件的窗体，需要做3件事情。
 * 1.params.type 设置为可以相应触摸事件
 * 2.修改窗体类型  电话优先级窗体类型TYPE_PRIORITY_PHONE
 * 3.设置权限  android.permission.SYSTEM_ALERT_WINDOW
 * @author Administrator
 *
 */
public class CallAddressService extends Service {
	protected static final String TAG = "CallAddressService";
	/**
	 * 定义一个电话状态的管理器
	 */
	private TelephonyManager tm;
	/**
	 * 电话状态的监听器
	 */
	private MyPhoneListener listener;

	private OutCallInnerReceiver receiver;

	/**
	 * 系统窗体的管理器
	 */
	private WindowManager mWm;

	/**
	 * 类的成员变量 显示出来的土司的view对象
	 */
	private View view;

	private WindowManager.LayoutParams params;
	
	private SharedPreferences sp;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 在服务的内部创建了一个广播接受者，希望广播接受者的存活周期跟服务一致。
	 * 
	 * @author Administrator
	 * 
	 */
	private class OutCallInnerReceiver extends BroadcastReceiver {
		private static final String TAG = "CallAddressService";

		@Override
		public void onReceive(Context context, Intent intent) {
			String number = getResultData();
			Log.i(TAG, "有新的电话打出去了，号码是：" + number);
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
		// 初始化窗体管理器
		mWm = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		receiver = new OutCallInnerReceiver();
		// 过滤电话打出去的动作
		IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(receiver, filter);

		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyPhoneListener();
		// 让监听器监听电话呼叫状态的变化
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	long[] mHits = new long[2];
	/**
	 * 显示自定义的土司
	 * 
	 * @param address
	 *            电话号码的归属地
	 */
	public void showMyToast(String address) {
		view = View.inflate(this, R.layout.toast_address, null);
		view.setBackgroundResource(R.drawable.call_locate_orange);
		//给view对象注册一个点击事件
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);//数组向左移位操作
		           mHits[mHits.length-1] = SystemClock.uptimeMillis();
		           if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
		        	   params.x = mWm.getDefaultDisplay().getWidth()/2 - view.getWidth()/2;
		        	   mWm.updateViewLayout(view, params);
		        	   //记录下位置
		        	   Editor editor = sp.edit();
						editor.putInt("paramsx", params.x);
						editor.commit();
		           }
			}
		});
		//给view对象组成触摸的监听器
		view.setOnTouchListener(new OnTouchListener() {
			//手指在屏幕上第一次按下的初始坐标
			int startX ;
			int startY ;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN://手指按下屏幕 第一次接触屏幕
					Log.i(TAG,"手指按下屏幕");
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					Log.i(TAG,"startX ： "+startX);
					Log.i(TAG,"startY ： "+startY);
					break;

				case MotionEvent.ACTION_MOVE: //手指在屏幕上触摸移动
					Log.i(TAG,"手指在屏幕上移动");
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();
					Log.i(TAG,"newX ： "+newX);
					Log.i(TAG,"newY ： "+newY);
					int dx = newX - startX;
					int dy = newY - startY;
					Log.i(TAG,"手指水平方向偏移量dx ： "+dx);
					Log.i(TAG,"手指竖直方向偏移量dy ： "+dy);
					//立刻让控件也跟随着手指移动 dx dy。
					params.x+=dx;
					params.y+=dy;
					//边界判断
					if(params.x<0){//向左移出屏幕
						params.x =0;
					}
					if(params.y<0){//向上移出屏幕
						params.y = 0;
					}
					if(params.x> (mWm.getDefaultDisplay().getWidth()-view.getWidth())){//向右移出屏幕
						params.x = mWm.getDefaultDisplay().getWidth()-view.getWidth();
					}
					if(params.y> (mWm.getDefaultDisplay().getHeight()-view.getHeight())){//向右移出屏幕
						params.y = mWm.getDefaultDisplay().getHeight()-view.getHeight();
					}
					
					mWm.updateViewLayout(view, params);
					//重复第一步的操作 ，重新初始化手指的开始位置。
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
					
				case MotionEvent.ACTION_UP://手指离开屏幕一瞬间对应的动作
					Log.i(TAG,"手指离开屏幕");//手指离开屏幕的一瞬间 记录控件在屏幕上的位置
					Editor editor = sp.edit();
					editor.putInt("paramsx", params.x);
					editor.putInt("paramsy", params.y);
					editor.commit();
					break;
				}
				return false;//True if the listener has consumed the event, false otherwise.
				              //true 代表监听器 处理掉了这个事件，false监听器没有处理这个事件。
			}
		});

		
		TextView tv_address = (TextView) view.findViewById(R.id.tv_address);
		tv_address.setText(address);
		//土司显示的参数
		params = new WindowManager.LayoutParams();
		//对其方式
		params.gravity = Gravity.LEFT+Gravity.TOP;
		
		//指定距离屏幕左边的距离 必须与 Gravity.LEFT同时使用
		params.x = sp.getInt("paramsx", 0);
		//指定距离屏幕上边的距离 必须与 Gravity.TOP同时使用
		params.y = sp.getInt("paramsy", 0);
		
		// 土司的宽高
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		// 土司的参数 不可获取焦点 不可以别点击 保存屏幕常亮
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				//| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		// 半透明窗体
		params.format = PixelFormat.TRANSLUCENT;
		params.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;//改用电话优先级的窗体类型，这种类型可以相应触摸事件。
		mWm.addView(view, params);
	}

	private class MyPhoneListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:// 空闲状态。
				if (view != null) {
					mWm.removeView(view);
					view = null;
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING:// 来电 响铃状态 别人给你打电话
				String address = AddressDao.find(incomingNumber);
				if (!TextUtils.isEmpty(address)) {
					// Toast.makeText(getApplicationContext(), address,
					// 1).show();
					showMyToast(address);
				}
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:// 接通电话的状态 通话状态。

				break;
			}
		}

	}

	// 服务停止的时候调用的方法
	@Override
	public void onDestroy() {
		// 取消电话状态的监听
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
		// 服务停止取消注册广播接受者
		unregisterReceiver(receiver);
		receiver = null;

		super.onDestroy();
	}

}
