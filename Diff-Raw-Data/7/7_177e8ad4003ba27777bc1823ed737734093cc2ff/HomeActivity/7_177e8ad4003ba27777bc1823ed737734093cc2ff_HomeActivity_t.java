 
 package com.albaniliu.chuangxindemo.ui.home;
 
 import java.io.File;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.albaniliu.chuangxindemo.R;
 import com.albaniliu.chuangxindemo.util.BundleKeyWord;
 import com.albaniliu.chuangxindemo.util.Downloader;
 import com.albaniliu.chuangxindemo.util.HTTPClient;
 import com.albaniliu.chuangxindemo.util.ResourceUtils;
 import com.albaniliu.chuangxindemo.util.Utils;
 
 public class HomeActivity extends Activity {
     private static String TAG = "HomeActivity";
     private boolean classfiViewSet = false;
 
     private static Thread downloadThread;
 
     // private YoukuGallery banner;
     private ImageView[] pointImageViews;
     private LinearLayout point_container;
 
     int[] contentDesID = new int[] {
             R.string.beauty, R.string.creative,
             R.string.nonmainstream, R.string.cartoon, R.string.love,
             R.string.landscape, R.string.animation, R.string.star,
             R.string.plant, R.string.art, R.string.animal, R.string.colorful,
             R.string.car, R.string.man, R.string.sky, R.string.sport
     };
 
     public static final int MSG_CHECK_HOME_RESOURCE_LOADING = 1001;
     public static final int MSG_DOWNLOAD_FINISHED = 1002;
     public static final int MSG_DOWNLOAD_FAILED = 1003;
     public static int DEFAULT_BANNER_COUNT = 5;
 
     public static final String Id = "HomeActivity";
     private LayoutParams bottomParams;
 
     private ExecutorService service = null;
 
     private LinearLayout classfiView;
     private ProgressDialog  dialog;
     private JSONArray allDir;
     private int totalIndex;
     private Downloader downloader;
 
     private Handler mHandler = new Handler() {
 	    @Override
 	    public void handleMessage(Message msg) {
 		    super.handleMessage(msg);
 		    if (msg.what == MSG_DOWNLOAD_FINISHED) {
 		    	setDefaultClassfiView();
 		    	dialog.dismiss();
 		    	classfiView.setVisibility(View.VISIBLE);
 		    } else if (msg.what == MSG_DOWNLOAD_FAILED) {
 		        Toast.makeText(getBaseContext(), "下载失败", Toast.LENGTH_LONG).show();
 		    }
 	    }
     };
     
     private ServiceConnection mServiceConnection = new ServiceConnection() {
         //当我bindService时，让TextView显示MyService里getSystemTime()方法的返回值   
         public void onServiceConnected(ComponentName name, IBinder service) {  
             // TODO Auto-generated method stub
         	downloader = ((Downloader.MyBinder)service).getService();
         	Log.v(TAG, Boolean.toString(downloader.isFinished()));
         	if (downloader.isFinished()) {
 	        	allDir = downloader.getAllDir();
 	        	mHandler.sendEmptyMessageDelayed(MSG_DOWNLOAD_FINISHED, 200);
         	}
         }  
           
         public void onServiceDisconnected(ComponentName name) {  
             // TODO Auto-generated method stub  
               
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Log.i("HomeActivity", "onCreate");
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.home_activity);
         ResourceUtils.setContext(this);
         service = Executors.newSingleThreadExecutor();
         classfiView = (LinearLayout) this.findViewById(R.id.classfi_view);
         
         if (downloadThread == null) {
 //	        downloadThread = new DownloadThread();
 //	        downloadThread.start();
         }
         classfiView.setVisibility(View.GONE);
         if (dialog == null) {
             dialog = new ProgressDialog(HomeActivity.this);
             dialog.setTitle("please wait");
             dialog.setMessage("downloading!!");
         }
         dialog.show();
         this.startService(new Intent(this , Downloader.class));
         Intent i  = new Intent();
         i.setClass(HomeActivity.this, Downloader.class);
        allDir = new JSONArray();
         this.bindService(i, mServiceConnection, BIND_AUTO_CREATE);
     }
 
     private void setDefaultClassfiView() {
         classfiView.removeAllViews();
         totalIndex = 0;
         int line = 0;
         while (totalIndex < allDir.length()) {
             setDefaultClassfiLine(line++);
         }
     }
 
     private void setDefaultClassfiLine(final int line) {
         LinearLayout classfiLine = (LinearLayout) getLayoutInflater().inflate(
                 R.layout.classfi_line, null);
         int screenWidth = getWindow().getWindowManager().getDefaultDisplay()
                 .getWidth();
         int screenHeight = getWindow().getWindowManager().getDefaultDisplay()
                 .getHeight();
         int num = 2;
         if (screenWidth > screenHeight)
             num = 4;
         for (int i = 0; i < num && totalIndex < allDir.length(); i++, totalIndex++) {
             LinearLayout classfiImage = (LinearLayout) getLayoutInflater().inflate(
                     R.layout.classfi_image, null);
             FrameLayout frame = (FrameLayout) classfiImage.findViewById(R.id.left);
             frame.setPadding(5, 1, 5, 1);
             frame.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     Bundle bundle = new Bundle();
                     bundle.putInt(BundleKeyWord.KEY_TYPE, line * 2 + 1);
                     Intent it = new Intent(HomeActivity.this, ImageGridActivity.class);
                     startActivity(it);
                 }
                 
             });
             try {
             	JSONObject obj = (JSONObject) allDir.get(totalIndex);
 	            ImageView image = (ImageView) classfiImage.findViewById(R.id.image_left);
 	            String coverPath = obj.getString("cover");
                 String coverName = coverPath.substring(coverPath.lastIndexOf('/') + 1);
 	            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1/"
 	                    + coverName;
 	            Bitmap bitmap = Utils.createBitmapByFilePath(fileName, 200);
 	            image.setImageBitmap(bitmap);
 	            
 	            TextView txt = (TextView) classfiImage.findViewById(R.id.des);
 				txt.setText(obj.getString("name"));
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             classfiLine.addView(classfiImage);
         }
 
         classfiView.addView(classfiLine);
     }
 
     @Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 //		downloadThread.interrupt();
 	}
 
 	class DownloadThread extends Thread {
         public void run() {
             try {
                 allDir = HTTPClient.getJSONArrayFromUrl(HTTPClient.URL_INDEX);
                 for (int i = 0; i < allDir.length() && !Thread.currentThread().isInterrupted(); i++) {
                     JSONObject obj = (JSONObject) allDir.get(i);
                     String cover = HTTPClient.COVER_INDEX_PREFIX + obj.getString("cover");
                     Log.v(TAG, obj.getString("cover"));
                     String coverPath = obj.getString("cover");
                     String coverName = coverPath.substring(coverPath.lastIndexOf('/') + 1);
                     Log.v(TAG, coverName);
                     String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1/"
                             + coverName;
                     File file = new File(fileName);
                     if (file.exists()) {
                         // 
                     } else {
                     	HTTPClient.getStreamFromUrl(cover, fileName);
                     }
                 }
                 mHandler.sendEmptyMessageDelayed(MSG_DOWNLOAD_FINISHED, 200);
             } catch (Exception e) {
                 mHandler.sendEmptyMessage(MSG_DOWNLOAD_FAILED);
                 e.printStackTrace();
             }
         }
     }
 }
