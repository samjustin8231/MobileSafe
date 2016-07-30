package com.itheima.mobilesafe.activities;

import java.io.File;
import java.io.FileInputStream;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.engine.SmsTools;
import com.itheima.mobilesafe.engine.SmsTools.BackupSmsCallback;

public class AtoolsActivity extends Activity {
	//进度条对话框
	private ProgressDialog pd;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atools);
	}
	
	/**
	 * 号码归属地查询
	 * @param view
	 */
	public void numberAddressQuery(View view){
		Intent intent = new Intent(this,NumberQueryActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 短信的备份
	 * @param view
	 */
	public void smsBackUp(View view){
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在备份短信");
		pd.show();
		new Thread(){
			public void run() {
				try {
					SmsTools.backupSms(getApplicationContext(), new BackupSmsCallback() {
						
						@Override
						public void onSmsBackup(int progress) {
							pd.setProgress(progress);
						}
						
						@Override
						public void beforeSmsBackup(int max) {
							pd.setMax(max);
						}
					});
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(AtoolsActivity.this, "恭喜你，备份成功", 0).show();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(AtoolsActivity.this, "备份失败", 0).show();
						}
					});
			    }finally{
			    	pd.dismiss();
			    }
			};
		}.start();
	}
	
	/**
	 * 短信的还原
	 * @param view
	 */
	public void smsRestore(View view){
		//1.读取sd卡备份的短信文件
	/*	File file = new File(Environment.getDataDirectory(),"backup.xml");
		FileInputStream fis = new FileInputStream(file);
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(fis, "utf-8");
		
		int type = parser.getEventType();
		while(type!=XmlPullParser.END_DOCUMENT){
			//读取解析每个短信的数据 短信的body address type date
			
			type = parser.next();
		}*/
		
		ContentResolver resolver = getContentResolver();
		Uri uri = Uri.parse("content://sms/");
		ContentValues values = new ContentValues();
		values.put("body", "赶紧来报道。");
		values.put("address", "110");
		values.put("type", "2");
		values.put("date", System.currentTimeMillis());
		resolver.insert(uri, values);
		Toast.makeText(this, "还原了一条短信", 0).show();
	}
}
