 package com.bspif.app.mobilemechanic;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.json.JSONException;
 
 import com.google.ads.AdView;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	/** Called when the activity is first created. */
 	
 	private static final String TAG = "Main";
 	
 	private ViewGroup contentView = null;
 	private Thread mBackgroundThread = null;
 	private BackgroudRunnable mRunnable = null;
 	
 	
 	private class BackgroudRunnable implements Runnable {
 		Context context = null;
 		boolean mIsRunning = true;
 		
 		public BackgroudRunnable(Context context) {
 			this.context = context;
 		}
 		
 		public void stop() {
 			mIsRunning = false;
 		}
 		
 		private String removeHTMLHead(String text) {
 			String lower = text.toLowerCase();
 			int index = lower.indexOf("<html>");
 			if (index != -1) {
 				text = text.substring(index + "<html>".length());
 			}
 			lower = text.toLowerCase();
 			index = lower.indexOf("</html>");
 			if (index != -1) {
 				text = text.substring(0, index);
 			}
 			return text;
 		}
 		
 		private void downloadImages() {
 			// download images
 			for (int i = 0; i < AppData.categories.length && mIsRunning; i++) {
 				AppData.CategoryData catData = AppData.getCategory(i);
 				if (null == catData || null == catData.lessons) continue;
 				for (int j = 0; j < catData.lessons.length && mIsRunning; j++) {
 					AppData.LessonData lessonData = catData.getLesson(j);
 					if (null == lessonData || null == lessonData.pages) continue;
 					for (int k = 0; k < lessonData.pages.length && mIsRunning; k++) {
 						AppData.PageData pageData = lessonData.getPage(k);
 						if (pageData == null || pageData.image == null || pageData.image.equals("")) {
 							continue;
 						}
 						String hdImgUrl = Global.HD_IMAEG_BASE_URL.concat(pageData.image);
 						String hdImgPath = Global.HD_IMAEG_DIR.concat("/" + pageData.image);
 						File file = new File(hdImgPath);
 						if (file.exists()) {
 							continue;
 						}
 						File dir = new File(Global.HD_IMAEG_DIR);
 						if (!dir.exists()) {
 							dir.mkdirs();
 						}
 						File nomedia = new File(Global.HD_IMAEG_DIR + "/" + ".nomedia");
 						if (dir.isDirectory() && !nomedia.exists()) {
 							try {
 								nomedia.createNewFile();
 							} catch (IOException e) {
 							}
 						}
 						String tempImage = Global.HD_IMAEG_DIR.concat("/" + pageData.image + ".tmp");
 						int result = Util.httpDownloadToSdcard(hdImgUrl, Global.HD_IMAEG_DIR, pageData.image + ".tmp");
 						File tmpfile = new File(tempImage);
 						if (0 == result) {
 							tmpfile.renameTo(file);
 						} else {
 							tmpfile.deleteOnExit();
 						}
 						try {
 							Thread.sleep(300);
 						} catch (InterruptedException e) {
 						}
 					}
 				}
 			}// end download
 		}
 		
 		public void run() {
 			if (Util.CheckNetworkState(context)) {
 				// loading html
 				File loadingHtmlFile = new File(Global.LOADING_HTML_FILE);
 				if (!loadingHtmlFile.exists()) {
 					String htmlContent = Util.httpRead(Global.LOADING_HTML_URL);
 					Log.v(TAG, "%s", htmlContent);
 					if (htmlContent != null) {
 						Util.writeToFile(context, htmlContent, Global.LOADING_HTML_FILE);
 					}
 				}
 				
 				// twitter facebook 
 				String twitterShareText = Util.httpRead(Global.URL_TWITTER_SHARE_TEXT);
 				String facebookShareText = Util.httpRead(Global.URL_FACEBOOK_SHARE_TEXT);
 				if (null != twitterShareText && !twitterShareText.equals("")) {
 					try {
 						String text = removeHTMLHead(twitterShareText);
 						Log.d(TAG, text);
 						AppData.put(AppData.JSON_DATA_TWITTER_SHARE_TEXT, text);
						AppData.saveData();
 					} catch (JSONException e) {
 					}
 				}
 				if (null != facebookShareText && !facebookShareText.equals("")) {
 					try {
 						String text = removeHTMLHead(facebookShareText);
 						Log.d(TAG, text);
 						AppData.put(AppData.JSON_DATA_FACEBOOK_SHARE_TEXT, text);
						AppData.saveData();
 					} catch (JSONException e) {
 					}
 				}
 				
 				downloadImages();
 			}
 		}
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
         ImageView btn = (ImageView) this.findViewById(R.id.main_btn);
         btn.setOnClickListener(this);
         
         Global.instance.newAdView(this);
         
 		WebView webView = (WebView) this.findViewById(R.id.webView);
 		String loadingHTML = Util.readFromFile(this, Global.LOADING_HTML_FILE);
 		webView.loadData(loadingHTML, "text/html", "utf-8");
 		webView.setBackgroundColor(0);
         
 		mRunnable = new BackgroudRunnable(this);
 		mBackgroundThread = new Thread(mRunnable);
 		mBackgroundThread.start();
 		ActionReceiver.startService(this);
 		
         contentView = (ViewGroup) this.findViewById(R.id.main_view);
     }
 
 	@Override
 	protected void onResume() {
 //		if (!AppData.isPurchased) {
 //			AdView adView = Global.instance.getAdView();
 //			contentView.addView(adView);
 //		}
 		super.onResume();
 	}
 	
     @Override
 	protected void onDestroy() {
     	//if (mBackgroundThread != null && mBackgroundThread.isAlive())
     		//mBackgroundThread.stop();
     	if (null != mRunnable)
     		mRunnable.stop();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onPause() {
 //    	AdView adView = Global.instance.getAdView();
 //    	contentView.removeView(adView);
 		super.onPause();
 	}
 	
 	public void onClick(View v) {
 		Intent it = new Intent(this, CategoryListActivity.class);
 		this.startActivity(it);
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {  
         super.onConfigurationChanged(newConfig);  
         if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {  
     		// DO NOTHING
         } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {  
         	// DO NOTHING
         }  
 	}
 }
