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
 * �������������ɾ�Ĳ�ҵ����
 * @author Administrator
 *
 */
public class BlackNumberDao {
	private BlacknumberDBOpenHelper helper;

	public BlackNumberDao(Context context) {
		helper = new BlacknumberDBOpenHelper(context);
	}
	/**
	 * ��Ӻ���������
	 * @param number ����
	 * @param mode ����ģʽ
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
	 * ɾ������������
	 * @param number Ҫɾ���ĺ���
	 */
	public void delete(String number){
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("info", "number=?", new String[]{number});
		db.close();
	}
	/**
	 * �޸ĺ��������������ģʽ
	 * @param number Ҫ�޸ĵĺ���������
	 * @param newmode �µ�����ģʽ
	 */
	public void update(String number,String newmode){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", newmode);
		db.update("info", values, "number=?", new String[]{number});
		db.close();
	}
	/**
	 * ��ѯ���������������ģʽ
	 * @param number Ҫ��ѯ�ĺ���������
	 * @return  mode ����ģʽ ����null������Ǻ��������벻����
	 */
	public String findMode(String number){
		String mode = null;
		SQLiteDatabase db = helper.getReadableDatabase();//��ȡֻ�������ݿ�
		Cursor cursor = db.query("info", new String[]{"mode"}, "number=?", new String[]{number}, null, null, null);
		if(cursor.moveToNext()){
			mode = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return mode;
	}
	/**
	 * ��ѯ����ȫ���ĺ���������
	 * @return
	 */
	public List<BlackNumber> findAll(){
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SQLiteDatabase db = helper.getReadableDatabase();//��ȡֻ�������ݿ�
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
	 * ��ѯ���ֵĺ��������� 
	 * @param maxNumber ��෵�ض���������
	 * @param startIndex ���ĸ�λ�ÿ�ʼ��ȡ����
	 * @return
	 */
	public List<BlackNumber> findPart(int maxNumber,int startIndex){
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SQLiteDatabase db = helper.getReadableDatabase();//��ȡֻ�������ݿ�
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
