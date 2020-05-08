 package com.zrd.zr.letuwb;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import com.zrd.zr.letuwb.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class UpdateActivity extends Activity {
 	private final static int MSG_DOWNLOAD_PREPARE = 0;
 	private final static int MSG_DOWNLOAD_BEGIN = 1;
 	private final static int MSG_DOWNLOADING = 2;
 	private final static int MSG_DOWNLOAD_END = 3;
 	private final static int MSG_DOWNLOAD_NETWORKPROBLEM = -1;
 	TextView mTextUpdate;
 	ProgressBar mProgressUpdate;
 	boolean mIsDownloading = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.update);
 		mTextUpdate = (TextView) findViewById(R.id.tvUpdate);
 		mProgressUpdate = (ProgressBar) findViewById(R.id.pbUpdate);
 		Intent intent = getIntent();
 		String sVerCode = intent.getStringExtra("code");
 		String sVerName = intent.getStringExtra("name");
 		String sNewVerName = intent.getStringExtra("newname");
 		if (sVerCode == null || sVerName == null) {
 			Toast.makeText(
 				this,
 				R.string.err_noversion,
 				Toast.LENGTH_LONG
 			).show();
 			this.finish();
 		}
 		Dialog dialog = new AlertDialog.Builder(UpdateActivity.this)
 			.setIcon(android.R.drawable.ic_dialog_info)
 			.setTitle(R.string.update_title)
 			.setMessage(String.format(getString(R.string.tips_update), sVerName, sNewVerName))
 			.setPositiveButton(R.string.label_ok,
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog,
 							int which) {
 						final Handler handler =  new Handler() {
 
 							@Override
 							public void handleMessage(Message msg) {
 								// TODO Auto-generated method stub
 								switch (msg.what) {
 								case MSG_DOWNLOAD_PREPARE:
 									mTextUpdate.setText(R.string.tips_connecting);
 									mIsDownloading = false;
 									break;
 								case MSG_DOWNLOAD_BEGIN:
 									mIsDownloading = false;
 									break;
 								case MSG_DOWNLOADING:
 									Integer[] sizes = (Integer[]) msg.obj;
 									mTextUpdate.setText(sizes[0] * 100 / sizes[1] + "% " + getString(R.string.tips_downloading));
 									mIsDownloading = true;
 									break;
 								case MSG_DOWNLOAD_END:
 									String filepath = (String) msg.obj;
 									update(filepath);
 									mIsDownloading = false;
 									UpdateActivity.this.finish();
 									break;
 								case MSG_DOWNLOAD_NETWORKPROBLEM:
 									mProgressUpdate.setVisibility(ProgressBar.GONE);
 									mTextUpdate.setText(R.string.err_noconnection);
 									mIsDownloading = false;
 									break;
 								}
 							}
 							
 						};
 						new Thread() {
 							public void run() {
								String url = EntranceActivity.URL_UPDATE + "letusee.apk";
								String filepath = "/sdcard/letusee.apk";
 								sendMsg(handler, MSG_DOWNLOAD_PREPARE, null);
 								URL myURL;
 								try {
 									myURL = new URL(url);
 									URLConnection conn = myURL.openConnection();
 									conn.connect();
 									InputStream is = conn.getInputStream();
 									int fileSize = conn.getContentLength();// get file size
 									if (fileSize <= 0)
 										throw new RuntimeException(getString(R.string.err_nofilesize));
 									if (is == null)
 										throw new RuntimeException(getString(R.string.err_noinputstream));
 									FileOutputStream fos = new FileOutputStream(filepath);
 									byte buf[] = new byte[8192];
 									int downLoadFileSize = 0;
 									sendMsg(handler, MSG_DOWNLOAD_BEGIN, null);
 									do {
 										int numread = is.read(buf);
 										if (numread == -1) {
 											break;
 										}
 										fos.write(buf, 0, numread);
 										downLoadFileSize += numread;
 
 										sendMsg(handler, MSG_DOWNLOADING, new Integer[] {downLoadFileSize, fileSize});
 									} while (true);
 									sendMsg(handler, MSG_DOWNLOAD_END, filepath);
 									try {
 										is.close();
 									} catch (Exception ex) {
 										Log.e("tag", "error: " + ex.getMessage(), ex);
 									}
 								} catch (MalformedURLException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 									sendMsg(handler, MSG_DOWNLOAD_NETWORKPROBLEM, null);
 								} catch (IOException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 									sendMsg(handler, MSG_DOWNLOAD_NETWORKPROBLEM, null);
 								}
 							}
 
 						}.start();
 					}
 
 				}
 			)
 			.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					UpdateActivity.this.finish();
 				}
 			}).create();
 		dialog.show();
 
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (mIsDownloading) {
 				Toast.makeText(
 					this,
 					R.string.tips_isdownloading,
 					Toast.LENGTH_LONG
 				).show();
 				return true;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	void update(String filepath) {
 
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		intent.setDataAndType(Uri.fromFile(new File(filepath)),
 			"application/vnd.android.package-archive");
 		startActivity(intent);
 	}
 
 	private void sendMsg(Handler handler, int flag, Object obj) {
 		Message msg = new Message();
 		msg.what = flag;
 		msg.obj = obj;
 		handler.sendMessage(msg);
 	}
 }
