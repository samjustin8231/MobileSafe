package com.itheima.mobilesafe.ui;

import com.itheima.mobilesafe.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingView extends RelativeLayout {
	private TextView tv_title;
	private TextView tv_desc;
	private CheckBox cb_status;
	private String title;
	private String desc_on;
	private String desc_off;
	
	public SettingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		View.inflate(context, R.layout.ui_setting_view, this);
		tv_desc = (TextView) this.findViewById(R.id.tv_desc);
		tv_title = (TextView) this.findViewById(R.id.tv_title);
		cb_status = (CheckBox) findViewById(R.id.cb_status);
		title = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.itheima.mobilesafe", "title");
		desc_on = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.itheima.mobilesafe", "desc_on");
		desc_off = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.itheima.mobilesafe", "desc_off");
		tv_title.setText(title);
		tv_desc.setText(desc_off);
	}
	
	/**
	 * �ж���Ͽؼ��Ƿ񱻵��
	 * @return
	 */
	public boolean isChecked(){
		return cb_status.isChecked();
	}
	/**
	 * ������Ͽؼ��Ĺ�ѡ״̬
	 * @param checked
	 */
	public void setChecked(boolean checked){
		cb_status.setChecked(checked);
		if(checked){
			tv_desc.setText(desc_on);
			tv_desc.setTextColor(0x99000000);
		}else{
			tv_desc.setText(desc_off);
			tv_desc.setTextColor(Color.RED);
		}
	}
	
}
