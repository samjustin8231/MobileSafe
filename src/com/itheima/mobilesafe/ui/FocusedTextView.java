package com.itheima.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FocusedTextView extends TextView {

	public FocusedTextView(Context context) {
		super(context);
	}

	//属性集 xml文件的配置
	public FocusedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 欺骗了系统，以为我们这个textview是得到焦点了。
	 */
	@Override
	public boolean isFocused() {
		return true;
	}
}
