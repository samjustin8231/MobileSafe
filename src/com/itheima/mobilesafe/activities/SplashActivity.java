package com.itheima.mobilesafe.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.service.CallAddressService;
import com.itheima.mobilesafe.utils.StreamTools;

public class SplashActivity extends Activity {
	protected static final String TAG = "SplashActivity";
	protected static final int SERVER_CODE_ERROR = 2001;
	protected static final int URL_ERROR = 2002;
	protected static final int NETWORK_ERROR = 2003;
	protected static final int JSON_ERROR = 2004;
	protected static final int SHOW_UPDATE_DIALOG = 2005;
	private TextView tv_splash_version;
	private TextView tv_splash_progress;
	private RelativeLayout rl_splash_root;
	private String description;
	private String path;
	private SharedPreferences sp;

	// 创建一个消息处理器
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SERVER_CODE_ERROR:
				Toast.makeText(getApplicationContext(),
						"获取更新信息失败，错误码：" + SERVER_CODE_ERROR, 1).show();
				loadMainUI();
				break;
			case URL_ERROR:
				Toast.makeText(getApplicationContext(),
						"获取更新信息失败，错误码：" + URL_ERROR, 1).show();
				loadMainUI();
				break;
			case NETWORK_ERROR:
				Toast.makeText(getApplicationContext(), "您的网络不给力啊，再试下下哈哈", 1)
						.show();
				loadMainUI();
				break;
			case JSON_ERROR:
				Toast.makeText(getApplicationContext(),
						"获取更新信息失败，错误码：" + JSON_ERROR, 1).show();
				loadMainUI();
				break;
			case SHOW_UPDATE_DIALOG:
				showUpdateDialog();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		rl_splash_root = (RelativeLayout) findViewById(R.id.rl_splash_root);
		tv_splash_progress = (TextView) findViewById(R.id.tv_splash_progress);
		String version = getVersion();
		tv_splash_version.setText("版本号:" + version);
		// 播放一个动画效果。
		playAnimation();

		// 判断是否开启了自动更新检查
		boolean autoupdate = sp.getBoolean("autoupdate", false);
		if (autoupdate) {
			// 连接服务器检查更新信息
			checkVersion();
		}else{//自动更新关闭的。
			handler.postDelayed(new  Runnable() {
				@Override
				public void run() {
					loadMainUI();
				}
			}, 2000);
		}
		// 在应用程序打开的splash界面里面 完成 数据库文件的初始化。
		copyDB();

		//给手机卫士创建一个快捷图标
		createShortCut();
		
	}

	/**
	 * 创建快捷图标
	 */
	private void createShortCut() {
		boolean shortcut = sp.getBoolean("shortcut", false);
		if(shortcut){//已经创建过快捷方式
			return;
		}
		Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		//1.指定快捷方式的名称
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "黑马卫士");
		//2.指定快捷方式的图标
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		//3.指定快捷方式的动作
		//显示意图。显示指定了激活的组件
		//Intent i = new Intent(this,HomeActivity.class);
		Intent i = new Intent();
		i.setAction("com.itheima.mobilesafe.home");
		i.addCategory("android.intent.category.DEFAULT");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
		sendBroadcast(intent);
		Editor editor = sp.edit();
		editor.putBoolean("shortcut", true);
		editor.commit();
	}

	/**
	 * 初始化号码归属地的数据库
	 */
	private void copyDB() {
		try {
			File file = new File(getFilesDir(), "address.db");
			if (file.exists() && file.length() > 0) {
				Log.i(TAG, "数据库已经存在无需拷贝");
			} else {
				Log.i(TAG, "拷贝号码归属地的数据库");
				InputStream is = getAssets().open("address.db");

				FileOutputStream fos = new FileOutputStream(file);
				int len = 0;
				byte[] buffer = new byte[1024];
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 显示更新提醒对话框
	 */
	protected void showUpdateDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				loadMainUI();
			}
		});
		builder.setTitle("更新提醒");
		builder.setMessage(description);
		builder.setPositiveButton("立刻升级", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 判断sd卡是否存在
				// 下载apk，替换安装。
				FinalHttp fh = new FinalHttp();
				// 调用download方法开始下载
				HttpHandler handler = fh.download(path, Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/xx.apk", false, new AjaxCallBack<File>() {
					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);
						int progress = (int) (current * 100 / count);
						tv_splash_progress.setText(("下载进度：" + progress + "%"));
					}

					@Override
					public void onSuccess(File t) {
						super.onSuccess(t);
						Toast.makeText(getApplicationContext(), "下载成功，替换安装", 0)
								.show();
						// <action android:name="android.intent.action.VIEW" />
						// <category
						// android:name="android.intent.category.DEFAULT" />
						// <data android:scheme="content" />
						// <data android:scheme="file" />
						// <data
						// android:mimeType="application/vnd.android.package-archive"
						// />
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");
						intent.addCategory("android.intent.category.DEFAULT");
						// intent.setType("application/vnd.android.package-archive");
						// intent.setData(Uri.fromFile(t));
						intent.setDataAndType(Uri.fromFile(t),
								"application/vnd.android.package-archive");
						startActivity(intent);
					}

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						Toast.makeText(getApplicationContext(), "下载失败", 0)
								.show();
						loadMainUI();
					}
				});
			}
		});
		builder.setNegativeButton("下次再说", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				loadMainUI();
			}
		});
		builder.show();
	}

	/**
	 * 进入主界面
	 */
	protected void loadMainUI() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();// 关闭掉splash界面。
	}

	/**
	 * 连接服务器检查更新信息 必须要在子线程实现。
	 */
	private void checkVersion() {
		new Thread() {
			public void run() {
				long startTime = System.currentTimeMillis();
				Message msg = Message.obtain();

				// 127.0.0.1 本地回环地址。 localhost
				try {
					URL url = new URL(getResources().getString(
							R.string.serverurl)); // http: https:// ftp://
													// svn://
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");// 区分大小写
					conn.setConnectTimeout(2000);// 设置连接超时时间
					// conn.setReadTimeout(5000); 读取超时时间。
					int code = conn.getResponseCode();// 获取服务器状态码
					if (code == 200) {// 请求成功
						InputStream is = conn.getInputStream();// json 字符串。
						String result = StreamTools.readStream(is);
						JSONObject jsonObj = new JSONObject(result);
						String version = (String) jsonObj.get("version");
						description = (String) jsonObj.get("description");
						path = jsonObj.getString("path");
						Log.i(TAG, "result:" + result);
						Log.i(TAG, "version:" + version);
						Log.i(TAG, "description:" + description);
						Log.i(TAG, "path:" + path);
						// 判断 服务器的版本号 和 客户端的版本号是否一致。
						if (getVersion().equals(version)) {
							Log.i(TAG, "版本号相同，进入主界面");
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							loadMainUI();
						} else {
							Log.i(TAG, "版本号不相同，弹出更新提醒对话框");
							msg.what = SHOW_UPDATE_DIALOG;
						}
					} else {
						// 状态码不正确。
						msg.what = SERVER_CODE_ERROR;
					}
				} catch (MalformedURLException e) {// 路径错误（协议错误了）//自定义一些错误码。
					e.printStackTrace();
					msg.what = URL_ERROR;
				} catch (NotFoundException e) {// 域名或者 路径找不到
					e.printStackTrace();
					msg.what = URL_ERROR;
				} catch (IOException e) { // 访问网络出错
					e.printStackTrace();
					msg.what = NETWORK_ERROR;
				} catch (JSONException e) { // 解析json文件出错了。
					e.printStackTrace();
					msg.what = JSON_ERROR;
				} finally {
					long endTime = System.currentTimeMillis();
					long dTime = endTime - startTime;
					if (dTime < 2000) {
						try {
							Thread.sleep(2000 - dTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.sendMessage(msg);
				}
			};
		}.start();
	}

	private void playAnimation() {
		AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
		aa.setDuration(2000);
		rl_splash_root.startAnimation(aa);
	}

	/**
	 * 获取应用程序的版本号信息
	 * 
	 * @return 应用程序的版本号
	 */
	private String getVersion() {
		// 获取一个系统的包管理器。
		PackageManager pm = getPackageManager();
		try {
			// 清单文件 manifest.xml文件的所有的信息
			PackageInfo packInfo = pm.getPackageInfo(getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			// can't reach 不可能发生的异常
			return "";
		}
	}
}
