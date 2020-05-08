 package cn.ingenic.updater;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.RecoverySystem;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 /**
  *  A activity dialog, to display update description,AND
  *  1, abandon this update (will delete the file of update)
  *  2, update later (add a notification, so can start update anytime)
  *  3, update now   (install update now, it will reboot now)
  */
 public class UpdateInstallActivity extends Activity implements OnClickListener {
 	private File mUpdateFile;
 	private UpdateInfo mUpdateInfo;
 	private TextView mText;
 	private String mUpdateDescription;
 	private boolean mSaveFile;
 	private int mClicked;
 	private NotificationManager mNotificationManager;
 	private static final String NOTIFICATION_TAG="update_tag";
 	private static final int NOTIFICATION_ID = 102;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_update_install);
 	    findViewById(R.id.abandon).setOnClickListener(this);
 	    findViewById(R.id.install).setOnClickListener(this);
 	    mText = (TextView)findViewById(R.id.update_info);
 	    mSaveFile = true;
         String sd_path = Environment.getExternalStorageDirectory().getPath();
         Intent intent = getIntent();
         if (intent.getStringExtra("update_file") != null) {
             String file_path = getIntent().getStringExtra("update_file");
             mUpdateInfo = (UpdateInfo) getIntent().getParcelableExtra(
                     "update_info");
             mUpdateFile = new File(file_path.substring(file_path
                     .indexOf(sd_path)));
             UpdateUtils.putStringToSP(this, "file_pa", file_path.substring(file_path
                     .indexOf(sd_path)));
         } else { // come from Notification 
             mUpdateInfo = UpdateUtils.getUpdateInfoCache(this);
             mUpdateFile = new File(UpdateUtils.getStringFromSP(this, "file_pa"));
         }
         String update_size = "";
         int size_kb = Integer.parseInt(mUpdateInfo.size) / 1024;
         if (size_kb < 1024)
             update_size = size_kb + " KB";
         else
             update_size = size_kb / 1024 + " MB";
         mUpdateDescription = getString(R.string.version) + mUpdateInfo.version_to
                 + " ( " + update_size + " )\n"
                 + getString(R.string.description) + mUpdateInfo.description + "\n";
         mText.setText(mUpdateDescription);
         Log.d("dfdun", "sd_path = " + sd_path);
         mNotificationManager = (NotificationManager) getSystemService("notification");
 	}
 
 	@Override
 	public void onStart(){
 	    super.onStart();
 	    mClicked = 0;
 	}
 	
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.abandon:
 		    mSaveFile = false;
 		    mClicked = 1;
 		    mNotificationManager.cancel(NOTIFICATION_TAG,NOTIFICATION_ID);
 			break;
 		case R.id.install:
 		    mSaveFile = true;
 		    mClicked = 3;
             if (((RadioButton) findViewById(R.id.install_now)).isChecked()){
                 try {
                     String md5 = getMD5(mUpdateFile);
                     if (mUpdateInfo.md5.equals(md5)) {
                         Toast.makeText(this, "MD5 OK!", Toast.LENGTH_SHORT)
                                 .show();
                     } else {
                         Log.d("dfdun", "md5 error! Right is " + mUpdateInfo.md5
                                 + "\n ");
                         Intent intent = new Intent(this, NoticesActivity.class);
                         intent.putExtra("msg",
                                 getString(R.string.check_file_failed));
                         startActivity(intent);
                     }
                    RecoverySystem.installPackage(this, mUpdateFile);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             } else {
                 updateLater();
             }
             break;
 		}
 		this.finish();
 	}
 
     @Override
     public void onStop() {
         super.onStop();
         if (!mSaveFile && mUpdateFile != null && mUpdateFile.exists()) {
             mUpdateFile.delete();
         }
         if (mClicked == 0) {
             updateLater();
         } 
     }
 /*
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         Log.i("dfdun", "keyCode = "+keyCode);
         if(keyCode == KeyEvent.KEYCODE_BACK){
             finish();
             //Intent intent = new Intent(this,NoticesActivity.class);
             //intent.putExtra("msg", getString(R.string.exit_message));
             startActivity(new Intent(this,NoticesActivity.class));
             return true;
         }
         return super.onKeyUp(keyCode, event);
     }
 */
     private void updateLater() {
         long when = System.currentTimeMillis();
         CharSequence contentTitle = getText(R.string.app_name);
         CharSequence contentText = getText(R.string.update_later_msg);
         
         Intent intent= new Intent(this,UpdateInstallActivity.class);
         PendingIntent cIntent=PendingIntent.getActivity(this, 0, intent, 0);
         Notification.Builder builder=new Notification.Builder(this);
         builder.setSmallIcon(R.drawable.ic_launcher)
         .setTicker(getString(R.string.update_later_msg))
         .setWhen(when+100)
         .setContentIntent(cIntent)
         .setContentTitle(contentTitle)
         .setContentText(contentText)
         .setAutoCancel(false)
         .setOngoing(true);
         mNotificationManager.notify(NOTIFICATION_TAG,NOTIFICATION_ID,builder.getNotification());
         UpdateUtils.putDownloadInfo(this, 0, mUpdateInfo);
 	}
     
     private String getMD5(File file){
         int errorCode = 0;
         char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'b', 'c', 'd', 'e', 'f' };
         try {
             FileInputStream in = new FileInputStream(file);
             errorCode = 10;
             FileChannel ch = in.getChannel();
             errorCode = 20;
             MessageDigest md = MessageDigest.getInstance("MD5");
             errorCode = 30;
             MappedByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY, 0, file
                     .length());
             errorCode = 40;
             md.update(bb);
             errorCode = 50;
             byte updateBytes[] = md.digest();
             errorCode = 60;
             int len = updateBytes.length;
             char myChar[] = new char[len * 2];
             int k = 0;
             errorCode = 70;
             for (int i = 0; i < len; i++) {
                 byte b = updateBytes[i];
                 myChar[k++] = hexDigits[b >>> 4 & 0x0f];
                 myChar[k++] = hexDigits[b & 0x0f];
             }
             errorCode = 80;
             in.close();
             ch.close();
             String md5 = new String(myChar).toUpperCase(Locale.ENGLISH);
             Log.d("dfdun", "MD5 of download = " + md5);
             return md5;
         } catch (IOException e) {
             Log.e("dfdun",  e.toString() + "Error Code = " + errorCode);
         } catch (NoSuchAlgorithmException e) {
             Log.e("dfdun",  e.toString() + "Error Code = " + errorCode);
         }
         return "";
     }
 }
