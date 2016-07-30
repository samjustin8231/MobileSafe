package com.itheima.mobilesafe.activities;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.db.dao.BlackNumberDao;
import com.itheima.mobilesafe.domain.BlackNumber;

public class CallSmsSafeActivity extends Activity {
	public static final String TAG = "CallSmsSafeActivity";
	private ListView lv_callsms_safe;
	private BlackNumberDao dao;
	/**
	 * 全部的黑名单号码
	 */
	private List<BlackNumber> blackNumbers;

	private CallSmsAdapter adapter;

	private LinearLayout ll_loading;

	/**
	 * 最多一次返回20条记录
	 */
	private static final int maxNumber = 20;

	private boolean isloading;
	
	/**
	 * 默认的开始位置
	 */
	private int startIndex = 0;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			ll_loading.setVisibility(View.INVISIBLE);
			if (adapter == null) {
				adapter = new CallSmsAdapter();
				lv_callsms_safe.setAdapter(adapter);
			}else{
				adapter.notifyDataSetChanged();//数据适配器已经存在，刷新界面
			}
			isloading = false;
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_callsms_safe);
		lv_callsms_safe = (ListView) findViewById(R.id.lv_callsms_safe);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		dao = new BlackNumberDao(this);
		fillData();

		lv_callsms_safe.setOnScrollListener(new OnScrollListener() {
			// 当滚动状态发生变化的时候调用的方法。
			// 静止--》拖动滚动
			// 拖动--》惯性滑动
			// 滑动--》静止
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:// 静止状态
					int position = lv_callsms_safe.getLastVisiblePosition();// 获取最后一个可见条目在listview集合里面位置。
					// position 的位置是从0开始的
					// 列表集合里面有20个条目
					if (position == (blackNumbers.size() - 1)) {
						System.out.println("列表被拖动到最后一个位置了。加载更多的数据。");
						if(isloading){
							Toast.makeText(getApplicationContext(), "正在加载，请稍后", 0).show();
							return;
						}
						startIndex += maxNumber;
						fillData();
					}

					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸滚动的状态

					break;
				case OnScrollListener.SCROLL_STATE_FLING:// 惯性滚动

					break;
				}
			}

			// 当listview滚动的时候调用的方法。
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

	}

	/**
	 * 填充获取数据
	 */
	private void fillData() {
		isloading = true;
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				if (blackNumbers == null) {// 第一次获取数据
					blackNumbers = dao.findPart(maxNumber, startIndex);
				} else {
					blackNumbers.addAll(dao.findPart(maxNumber, startIndex));
				}
				handler.sendEmptyMessage(0);
			};
		}.start();
	}

	private class CallSmsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return blackNumbers.size();
		}

		// 异常的原因 是单位时间内 getview方法创建对象的速度 > 回收对象的速度
		// converview 历史缓存的view对象，可以使用这个缓存的view对象 （检查是否为空 检查类型是否合适）
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			// 1.使用历史缓存的view对象 减少 布局创建的次数
			if (convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				// Log.i(TAG,"使用历史缓存的view对象："+position);
				holder = (ViewHolder) view.getTag();// 从口袋里面取出记事本
			} else {
				view = View.inflate(getApplicationContext(),
						R.layout.item_callsms_safe, null);
				// Log.i(TAG,"创建新的对象："+position);
				// 2.减少子孩子查询的次数，只是在创建子孩子的时候 获取孩子对象的引用
				holder = new ViewHolder();
				holder.tv_number = (TextView) view
						.findViewById(R.id.tv_black_number);
				holder.tv_mode = (TextView) view
						.findViewById(R.id.tv_block_mode);
				holder.iv_delete = (ImageView) view
						.findViewById(R.id.iv_delete);
				view.setTag(holder);// 把记事本放在父亲的口袋里
			}

			// 寻找子孩子的引用比较消耗资源。

			final BlackNumber blacknumber = blackNumbers.get(position);
			holder.tv_number.setText(blacknumber.getNumber());
			String mode = blacknumber.getMode();// 0全部拦截 1电话拦截 2短信拦截
			if ("0".equals(mode)) {
				holder.tv_mode.setText("全部拦截");
			} else if ("1".equals(mode)) {
				holder.tv_mode.setText("电话拦截");
			} else {
				holder.tv_mode.setText("短信拦截");
			}
			holder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new Builder(
							CallSmsSafeActivity.this);
					builder.setTitle("提醒：");
					builder.setMessage("确定要删除这条黑名单号码么？");
					builder.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dao.delete(blacknumber.getNumber());// 从数据库把条目删除
									blackNumbers.remove(blacknumber);
									adapter.notifyDataSetChanged();
								}
							});
					builder.setNegativeButton("取消", null);
					builder.show();
				}
			});

			return view;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	/**
	 * 定义view对象的容器，记事本用来保存孩子控件的引用。
	 * 
	 */
	static class ViewHolder {
		TextView tv_number;
		TextView tv_mode;
		ImageView iv_delete;
	}

	private Button bt_cancel;
	private Button bt_ok;
	private EditText et_blacknumber;
	private CheckBox cb_phone;
	private CheckBox cb_sms;

	/**
	 * 添加一条黑名单号码
	 * 
	 * @param view
	 */
	public void addBlackNumber(View view) {
		AlertDialog.Builder builder = new Builder(this);
		final AlertDialog dialog = builder.create();
		View contentView = View.inflate(getApplicationContext(),
				R.layout.dialog_add_blacknumber, null);
		dialog.setView(contentView, 0, 0, 0, 0);
		dialog.show();
		bt_cancel = (Button) contentView.findViewById(R.id.bt_cancel);
		bt_ok = (Button) contentView.findViewById(R.id.bt_ok);
		et_blacknumber = (EditText) contentView
				.findViewById(R.id.et_blacknumber);
		cb_phone = (CheckBox) contentView.findViewById(R.id.cb_phone);
		cb_sms = (CheckBox) contentView.findViewById(R.id.cb_sms);
		bt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String number = et_blacknumber.getText().toString().trim();
				if (TextUtils.isEmpty(number)) {
					Toast.makeText(getApplicationContext(), "黑名单号码不能为空", 0)
							.show();
					return;
				}
				String mode = null;
				;
				if (cb_phone.isChecked() && cb_sms.isChecked()) {
					mode = "0";
				} else if (cb_phone.isChecked()) {
					mode = "1";
				} else if (cb_sms.isChecked()) {
					mode = "2";
				}
				if (TextUtils.isEmpty(mode)) {
					Toast.makeText(getApplicationContext(), "请设置拦截模式", 0)
							.show();
					return;
				}
				dao.add(number, mode);// 黑名单号码添加到数据库。
				dialog.dismiss();
				// 添加数据到界面上。
				BlackNumber blackNumber = new BlackNumber();
				blackNumber.setMode(mode);
				blackNumber.setNumber(number);
				blackNumbers.add(0, blackNumber);
				// 通知listview界面更新了 刷新ui。
				adapter.notifyDataSetChanged();
			}
		});

	}
}
