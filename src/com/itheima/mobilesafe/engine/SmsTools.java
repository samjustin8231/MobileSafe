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
	 * 备份短信的回调接口
	 *
	 */
	public interface BackupSmsCallback{
		/**
		 * 短信备份前调用的方法
		 * @param max 一共有多少条短信需要备份
		 */
		public void beforeSmsBackup(int max);
		
		/**
		 * 短信备份中调用的方法
		 * @param progress 当前备份的进度
		 */
		public void onSmsBackup(int progress);
		
	}
	
	/**
	 * 备份用户的短信
	 * @param context 上下文
	 * @param BackupSmsCallback callback
	 */
	public static void backupSms(Context context,BackupSmsCallback callback) throws Exception{
		Uri uri = Uri.parse("content://sms/");//全部短信数据的内容提供者路径
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(uri, new String[]{"address","date","type","body"}, null, null, null);
		//设置一共备份多少条短信
		//在短信备份之前，需要知道有多少条短信要备份
		//progressBar1.setMax(cursor.getCount());
		//pd.setMax(cursor.getCount());
		callback.beforeSmsBackup(cursor.getCount());
		
		//创建一个xml文件的生成器
		XmlSerializer  serializer  = Xml.newSerializer();
		//判断sd卡是否可用。
		File file = new File(Environment.getExternalStorageDirectory(),"backup.xml");
		FileOutputStream os = new FileOutputStream(file);
		//初始化xml文件生成器
		serializer.setOutput(os, "utf-8");
		//写xml文件的开头
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
			//当前备份的进度
			//在备份的过程中，需要更新ui界面。告诉ui当前备份的进度
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
