package com.itheima.mobilesafe.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public abstract class BaseSetupActivity extends Activity {

	/**
	 * 在父类里面初始化了sharedpreference
	 */
	protected SharedPreferences sp;
	
	/**
	 * 1.父类里面声明一个手势识别器
	 */
	protected GestureDetector mGestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		//2.初始化手势识别器  第二个参数就是我们注册的手势识别的监听器
		mGestureDetector = new GestureDetector(this, new MyGestureListener());
		
	}
	
	/**
	 * 由子类实现的显示下一个界面
	 */
	protected abstract void showNext();
	
	/**
	 * 由子类实现的显示上一个界面
	 */
	protected abstract void showPre();
	
	
	private class MyGestureListener extends GestureDetector.SimpleOnGestureListener{

		//当用户在屏幕上手指乱动（滑动）的时候调用的方法。
		//e1手指接触到屏幕对应的动作
		//e2手指离开屏幕的一瞬间对应的动作
		//m/s km/h
		//velocityX 水平方向的速度  px/m
		//velocityY 竖直方向的速度
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			if(Math.abs(e1.getRawY() - e2.getRawY()) > 100 ){ //无效的动作{
				Toast.makeText(getApplicationContext(), "不带这么滑的", 0).show();
				return true;
			}
			
			if(e1.getRawX() - e2.getRawX() > 200){
				//显示下一个界面
				//父类不知道显示下一个界面的代码怎么写
				showNext();
				return true;
			}
			
			if(e2.getRawX() - e1.getRawX() > 200){
				//显示上一个界面。
				//父类不知道显示上一个界面的代码怎么写
				showPre();
				return true;
			}
			
			
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		
	}
	
	
	/**
	 * 加载显示一个activity
	 * @param cls 要显示的activity的字节码
	 */
	public void loadActivity(Class<?> cls){
		Intent intent = new Intent(this,cls);
		startActivity(intent);
		finish();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//3.用我们的手势识别器处理时间
		mGestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	
}
