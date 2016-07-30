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
	 * ȫ���ĺ���������
	 */
	private List<BlackNumber> blackNumbers;

	private CallSmsAdapter adapter;

	private LinearLayout ll_loading;

	/**
	 * ���һ�η���20����¼
	 */
	private static final int maxNumber = 20;

	private boolean isloading;
	
	/**
	 * Ĭ�ϵĿ�ʼλ��
	 */
	private int startIndex = 0;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			ll_loading.setVisibility(View.INVISIBLE);
			if (adapter == null) {
				adapter = new CallSmsAdapter();
				lv_callsms_safe.setAdapter(adapter);
			}else{
				adapter.notifyDataSetChanged();//�����������Ѿ����ڣ�ˢ�½���
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
			// ������״̬�����仯��ʱ����õķ�����
			// ��ֹ--���϶�����
			// �϶�--�����Ի���
			// ����--����ֹ
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:// ��ֹ״̬
					int position = lv_callsms_safe.getLastVisiblePosition();// ��ȡ���һ���ɼ���Ŀ��listview��������λ�á�
					// position ��λ���Ǵ�0��ʼ��
					// �б���������20����Ŀ
					if (position == (blackNumbers.size() - 1)) {
						System.out.println("�б��϶������һ��λ���ˡ����ظ�������ݡ�");
						if(isloading){
							Toast.makeText(getApplicationContext(), "���ڼ��أ����Ժ�", 0).show();
							return;
						}
						startIndex += maxNumber;
						fillData();
					}

					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// ����������״̬

					break;
				case OnScrollListener.SCROLL_STATE_FLING:// ���Թ���

					break;
				}
			}

			// ��listview������ʱ����õķ�����
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

	}

	/**
	 * ����ȡ����
	 */
	private void fillData() {
		isloading = true;
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				if (blackNumbers == null) {// ��һ�λ�ȡ����
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

		// �쳣��ԭ�� �ǵ�λʱ���� getview��������������ٶ� > ���ն�����ٶ�
		// converview ��ʷ�����view���󣬿���ʹ����������view���� ������Ƿ�Ϊ�� ��������Ƿ���ʣ�
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			// 1.ʹ����ʷ�����view���� ���� ���ִ����Ĵ���
			if (convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				// Log.i(TAG,"ʹ����ʷ�����view����"+position);
				holder = (ViewHolder) view.getTag();// �ӿڴ�����ȡ�����±�
			} else {
				view = View.inflate(getApplicationContext(),
						R.layout.item_callsms_safe, null);
				// Log.i(TAG,"�����µĶ���"+position);
				// 2.�����Ӻ��Ӳ�ѯ�Ĵ�����ֻ���ڴ����Ӻ��ӵ�ʱ�� ��ȡ���Ӷ��������
				holder = new ViewHolder();
				holder.tv_number = (TextView) view
						.findViewById(R.id.tv_black_number);
				holder.tv_mode = (TextView) view
						.findViewById(R.id.tv_block_mode);
				holder.iv_delete = (ImageView) view
						.findViewById(R.id.iv_delete);
				view.setTag(holder);// �Ѽ��±����ڸ��׵Ŀڴ���
			}

			// Ѱ���Ӻ��ӵ����ñȽ�������Դ��

			final BlackNumber blacknumber = blackNumbers.get(position);
			holder.tv_number.setText(blacknumber.getNumber());
			String mode = blacknumber.getMode();// 0ȫ������ 1�绰���� 2��������
			if ("0".equals(mode)) {
				holder.tv_mode.setText("ȫ������");
			} else if ("1".equals(mode)) {
				holder.tv_mode.setText("�绰����");
			} else {
				holder.tv_mode.setText("��������");
			}
			holder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new Builder(
							CallSmsSafeActivity.this);
					builder.setTitle("���ѣ�");
					builder.setMessage("ȷ��Ҫɾ����������������ô��");
					builder.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dao.delete(blacknumber.getNumber());// �����ݿ����Ŀɾ��
									blackNumbers.remove(blacknumber);
									adapter.notifyDataSetChanged();
								}
							});
					builder.setNegativeButton("ȡ��", null);
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
	 * ����view��������������±��������溢�ӿؼ������á�
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
	 * ���һ������������
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
					Toast.makeText(getApplicationContext(), "���������벻��Ϊ��", 0)
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
					Toast.makeText(getApplicationContext(), "����������ģʽ", 0)
							.show();
					return;
				}
				dao.add(number, mode);// ������������ӵ����ݿ⡣
				dialog.dismiss();
				// ������ݵ������ϡ�
				BlackNumber blackNumber = new BlackNumber();
				blackNumber.setMode(mode);
				blackNumber.setNumber(number);
				blackNumbers.add(0, blackNumber);
				// ֪ͨlistview��������� ˢ��ui��
				adapter.notifyDataSetChanged();
			}
		});

	}
}
