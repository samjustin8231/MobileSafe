package com.itheima.mobilesafe.engine;

import java.io.File;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlSerializer;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;
import android.widget.ProgressBar;

public class SmsTools {
	
	/**
	 * ���ݶ��ŵĻص��ӿ�
	 *
	 */
	public interface BackupSmsCallback{
		/**
		 * ���ű���ǰ���õķ���
		 * @param max һ���ж�����������Ҫ����
		 */
		public void beforeSmsBackup(int max);
		
		/**
		 * ���ű����е��õķ���
		 * @param progress ��ǰ���ݵĽ���
		 */
		public void onSmsBackup(int progress);
		
	}
	
	/**
	 * �����û��Ķ���
	 * @param context ������
	 * @param BackupSmsCallback callback
	 */
	public static void backupSms(Context context,BackupSmsCallback callback) throws Exception{
		Uri uri = Uri.parse("content://sms/");//ȫ���������ݵ������ṩ��·��
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(uri, new String[]{"address","date","type","body"}, null, null, null);
		//����һ�����ݶ���������
		//�ڶ��ű���֮ǰ����Ҫ֪���ж���������Ҫ����
		//progressBar1.setMax(cursor.getCount());
		//pd.setMax(cursor.getCount());
		callback.beforeSmsBackup(cursor.getCount());
		
		//����һ��xml�ļ���������
		XmlSerializer  serializer  = Xml.newSerializer();
		//�ж�sd���Ƿ���á�
		File file = new File(Environment.getExternalStorageDirectory(),"backup.xml");
		FileOutputStream os = new FileOutputStream(file);
		//��ʼ��xml�ļ�������
		serializer.setOutput(os, "utf-8");
		//дxml�ļ��Ŀ�ͷ
		serializer.startDocument("utf-8", true);
		serializer.startTag(null, "smss");
		int total = 0;
		while(cursor.moveToNext()){
			serializer.startTag(null, "sms");
			
			serializer.startTag(null, "address");
			String address = cursor.getString(0);
			serializer.text(address);
			serializer.endTag(null, "address");
			
			
			serializer.startTag(null, "date");
			String date = cursor.getString(1);
			serializer.text(date);
			serializer.endTag(null, "date");
			
			serializer.startTag(null, "type");
			String type = cursor.getString(2);
			serializer.text(type);
			serializer.endTag(null, "type");
			
			serializer.startTag(null, "body");
			String body = cursor.getString(3);
			serializer.text(body);
			serializer.endTag(null, "body");
			
			serializer.endTag(null, "sms");
			Thread.sleep(300);
			total++;
			//��ǰ���ݵĽ���
			//�ڱ��ݵĹ����У���Ҫ����ui���档����ui��ǰ���ݵĽ���
			//progressBar1.setProgress(total);
			//pd.setProgress(total);
			callback.onSmsBackup(total);
		}
		cursor.close();
		serializer.endTag(null, "smss");
		serializer.endDocument();
		os.close();
	}
}
