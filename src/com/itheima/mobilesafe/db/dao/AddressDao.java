package com.itheima.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * �绰������������ݿ�Ĳ�ѯ������
 * 
 * @author Administrator
 * 
 */
public class AddressDao {
	private static final String path = "/data/data/com.itheima.mobilesafe/files/address.db";

	/**
	 * ��ѯ�绰����Ĺ�����
	 * 
	 * @param number
	 *            �绰����
	 * @return ������λ����Ϣ
	 */
	public static String find(String number) {
		String location = null;
		// ��ʹ�����asset���ݿ��ļ�֮ǰ�����ļ��������ֻ����ڲ��洢ϵͳ����/data/data/����/filesĿ¼��

		// path ���ݿ��·��
		// ֱ�Ӳ���file:///android_asset·��д�� ֻ�Ƕ���ҳ��Դ��Ч����������ݿ� ��������д·��
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		// �ֻ�����Ƚϼ򵥡� 11λ 1��ͷ ���� 13x 14x 15x 18x
		// ������ʽ
		// �ֻ������ƥ��������ʽ ^1[3458]\d{9}$
		if (number.matches("^1[3458]\\d{9}$")) {// ������ʽ �ֻ�����
			Cursor cursor = db
					.rawQuery(
							"select location from data2 where id = (select outkey from data1 where id=?)",
							new String[] { number.substring(0, 7) });
			if (cursor.moveToNext()) {
				location = cursor.getString(0);
			}
			cursor.close();
		} else {// �������� 110 119 999
			switch (number.length()) {
			case 3:
				if ("110".equals(number)) {
					location = "�˾�";
				}
				break;
			case 4:
				location = "ģ����";
				break;
			case 5:
				location = "�ͷ��绰";
				break;
			case 7:
				location = "���ص绰";
				break;
			case 8:
				location = "���ص绰";
				break;
			// 0101234567
			// 02112343559  
			// 075532432449
			// 0086
		    // 00353
			default:
				if (number.length() >= 10 && number.startsWith("0")) {
					Cursor cursor = db.rawQuery("select location from data2 where area =?", new String[]{number.substring(1, 3)});
					if(cursor.moveToNext()){
						String temp  = cursor.getString(0);
						location = temp.substring(0, temp.length()-2);
					}
					cursor.close();
					cursor = db.rawQuery("select location from data2 where area =?", new String[]{number.substring(1, 4)});
					if(cursor.moveToNext()){
						String temp  = cursor.getString(0);
						location = temp.substring(0, temp.length()-2);
					}
					cursor.close();
				}
				break;
			}

		}
		db.close();
		return location;
	}
}
