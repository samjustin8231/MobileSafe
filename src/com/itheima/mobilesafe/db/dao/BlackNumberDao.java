package com.itheima.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.itheima.mobilesafe.db.BlacknumberDBOpenHelper;
import com.itheima.mobilesafe.domain.BlackNumber;

/**
 * 黑名单号码的增删改查业务类
 * @author Administrator
 *
 */
public class BlackNumberDao {
	private BlacknumberDBOpenHelper helper;

	public BlackNumberDao(Context context) {
		helper = new BlacknumberDBOpenHelper(context);
	}
	/**
	 * 添加黑名单号码
	 * @param number 号码
	 * @param mode 拦截模式
	 */
	public void add(String number,String mode){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("number", number);
		values.put("mode", mode);
		db.insert("info", null, values);
		db.close();
	}
	/**
	 * 删除黑名单号码
	 * @param number 要删除的号码
	 */
	public void delete(String number){
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("info", "number=?", new String[]{number});
		db.close();
	}
	/**
	 * 修改黑名单号码的拦截模式
	 * @param number 要修改的黑名单号码
	 * @param newmode 新的拦截模式
	 */
	public void update(String number,String newmode){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", newmode);
		db.update("info", values, "number=?", new String[]{number});
		db.close();
	}
	/**
	 * 查询黑名单号码的拦截模式
	 * @param number 要查询的黑名单号码
	 * @return  mode 拦截模式 返回null代表的是黑名单号码不存在
	 */
	public String findMode(String number){
		String mode = null;
		SQLiteDatabase db = helper.getReadableDatabase();//获取只读的数据库
		Cursor cursor = db.query("info", new String[]{"mode"}, "number=?", new String[]{number}, null, null, null);
		if(cursor.moveToNext()){
			mode = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return mode;
	}
	/**
	 * 查询返回全部的黑名单号码
	 * @return
	 */
	public List<BlackNumber> findAll(){
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SQLiteDatabase db = helper.getReadableDatabase();//获取只读的数据库
		Cursor cursor = db.query("info", new String[]{"number","mode"}, null, null, null, null, "_id desc");
		List<BlackNumber> blackNumbers = new ArrayList<BlackNumber>();
		while(cursor.moveToNext()){
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			BlackNumber blackNumber = new BlackNumber();
			blackNumber.setMode(mode);
			blackNumber.setNumber(number);
			blackNumbers.add(blackNumber);
		}
		cursor.close();
		db.close();
		return blackNumbers;
	}
	/**
	 * 查询部分的黑名单号码 
	 * @param maxNumber 最多返回多少条数据
	 * @param startIndex 从哪个位置开始获取数据
	 * @return
	 */
	public List<BlackNumber> findPart(int maxNumber,int startIndex){
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SQLiteDatabase db = helper.getReadableDatabase();//获取只读的数据库
		Cursor cursor = db.rawQuery("select number,mode from info order by _id desc limit ? offset ? ", new String[]{String.valueOf(maxNumber),String.valueOf(startIndex)});
		List<BlackNumber> blackNumbers = new ArrayList<BlackNumber>();
		while(cursor.moveToNext()){
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			BlackNumber blackNumber = new BlackNumber();
			blackNumber.setMode(mode);
			blackNumber.setNumber(number);
			blackNumbers.add(blackNumber);
		}
		cursor.close();
		db.close();
		return blackNumbers;
	}
}
