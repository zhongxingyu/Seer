 package com.zerolinux5.newssource;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.ViewFlipper;
 
 public class MainActivity extends Activity implements OnGestureListener {
 	private static final String PREFS_NAME = "BasicPreferences";
 	public static final int BUTTONTRUE = 1;
 	public static final String LABEL_NUMBER = "";
 	public static final String NEW_STRING = "";
 	public static String NEW_URL1 = "www.zerolinux5.com/";
 	public static String NEW_URL2 = "news.ycombinator.com/";	
 	public static String NEW_URL3 = "www.ubuntuvibes.com/";
 	public static String BUTTON_1 = "ZeroLinux5";
 	public static String BUTTON_2 = "Hacker News";
 	public static String BUTTON_3 = "Ubuntu Vibes";
 	public static String suspendUrl = "";
 	WebView myWebView;
 	ProgressBar progressBar;
 	Button slideHandleButton;
 	SlidingDrawer slidingDrawer;
 	
 	private GestureDetector myGesture;
 	private static final int SWIPE_MIN_DISTANCE = 250;
 	private static final int SWIPE_MAX_OFF_PATH = 200;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 150;
 	private static final int UP_SWIPE_MIN_DIST = 430;
 	
 
 	
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent e){
 	    super.dispatchTouchEvent(e);
 	    return myGesture.onTouchEvent(e);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		//set the webview, settings, saved preferences and the gesture detector
 		myWebView = (WebView) findViewById(R.id.webview);
 		WebSettings webSettings = myWebView.getSettings();
 		webSettings.setJavaScriptEnabled(true);
 		myWebView.setWebViewClient(new WebViewClient());
 		myGesture = new GestureDetector(this);
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0); 
 		//set the labels and urls to what was previously saved if anything was saved
 		String temp = settings.getString("new_url1", "");
 		if (temp.length() != 0)
 			NEW_URL1 = settings.getString("new_url1", "");
 		temp = settings.getString("new_url2", "");
 		if (temp.length() != 0)
 			NEW_URL2 = settings.getString("new_url2", "");
 		temp = settings.getString("new_url3", "");
 		if (temp.length() != 0)
 			NEW_URL3 = settings.getString("new_url3", "");
 		temp = settings.getString("new_button1", "");
 		if (temp.length() != 0)
 			BUTTON_1 = temp;
 		temp = settings.getString("new_button2", "");
 		if (temp.length() != 0)
 			BUTTON_2 = temp;
 		temp = settings.getString("new_button3", "");
 		if (temp.length() != 0)
 			BUTTON_3 = temp;
 		//set the buttons to correspond to their id
 		Button b1 = (Button) findViewById(R.id.button1);
 		b1.setText(BUTTON_1);
 		Button b2 = (Button) findViewById(R.id.button2);
 		b2.setText(BUTTON_2);
 		Button b3 = (Button) findViewById(R.id.button3);
 		b3.setText(BUTTON_3);
 		//set the progress bar and hide it also set the web client
 		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
 		progressBar.setVisibility(View.GONE);
 		myWebView.setWebViewClient(new myWebClient());
 
 		//set the sliding bar and its methods
 		slideHandleButton = (Button) findViewById(R.id.handle);
 		slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
 		
 		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener(){
 			@Override
 			public void onDrawerOpened() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 		
 		slidingDrawer.setOnDrawerCloseListener( new OnDrawerCloseListener(){
 			@Override
 			public void onDrawerClosed() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	public void buttonOne(View v){
 		WebView myWebView = (WebView) findViewById(R.id.webview);
 		myWebView.loadUrl("http://"+NEW_URL1);
 	}
 	
 	public void buttonTwo(View v){
 		WebView myWebView = (WebView) findViewById(R.id.webview);
 		myWebView.loadUrl("http://"+NEW_URL2);
 	}
 	
 	public void buttonThree(View v){
 		WebView myWebView = (WebView) findViewById(R.id.webview);
 		myWebView.loadUrl("http://"+NEW_URL3);
 	}
 	  
 	  public void menu(View v){
 		  	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		  	SharedPreferences.Editor editor = settings.edit(); 
 		  	editor.putString("new_url1", NEW_URL1); 
 		  	editor.putString("new_url2", NEW_URL2);
 		  	editor.putString("new_url3", NEW_URL3);
 		  	editor.putString("new_button1", BUTTON_1);
 		  	editor.putString("new_button2", BUTTON_2);
 		  	editor.putString("new_button3", BUTTON_3);
 		  	editor.commit();    
 	    	Intent intent = new Intent(MainActivity.this, MenuActivity.class);
 	    	startActivityForResult(intent, BUTTONTRUE);  
 	  }
 	  
 	    // Gets the return value.
 	    @Override
 	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	        super.onActivityResult(requestCode, resultCode, data);
 	    	if(requestCode == BUTTONTRUE){
 	    			if (resultCode == RESULT_OK) {
 
 	    				if(Integer.parseInt(data.getStringExtra(LABEL_NUMBER)) == 1){	
 	    					Button b = (Button) findViewById(R.id.button1);
 	    					if (MenuActivity.NEWSTRING.length() != 0){
 		    					b.setText(MenuActivity.NEWSTRING);
 		    					BUTTON_1 = MenuActivity.NEWSTRING;
 		    					NEW_URL1 = data.getStringExtra(NEW_URL1);
 	    					}
 	    				} 
 	    				if(Integer.parseInt(data.getStringExtra(LABEL_NUMBER)) == 2){	
 	    					Button b = (Button) findViewById(R.id.button2);
 	    					if (MenuActivity.NEWSTRING.length() != 0){
 		    					b.setText(MenuActivity.NEWSTRING);
 		    					NEW_URL2 = data.getStringExtra(NEW_URL2);
 		    					BUTTON_2 = MenuActivity.NEWSTRING;
 	    					}
 	    				} 
 	    				if(Integer.parseInt(data.getStringExtra(LABEL_NUMBER)) == 3){	
 	    					Button b = (Button) findViewById(R.id.button3);
 	    					if (MenuActivity.NEWSTRING.length() != 0){
 		    					b.setText(MenuActivity.NEWSTRING);
 		    					NEW_URL3 = data.getStringExtra(NEW_URL3);
 		    					BUTTON_3 = MenuActivity.NEWSTRING;
 	    					}
 	    				} 
 	    			}
 	    	}
 		  	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		  	SharedPreferences.Editor editor = settings.edit(); 
 		  	editor.putString("new_url1", NEW_URL1); 
 		  	editor.putString("new_url2", NEW_URL2);
 		  	editor.putString("new_url3", NEW_URL3);
 		  	editor.putString("new_button1", BUTTON_1);
 		  	editor.putString("new_button2", BUTTON_2);
 		  	editor.putString("new_button3", BUTTON_3);
 		  	editor.commit();
 		  	super.onPause();
 	    }
 	    
 	    public void share(View v){
 	    	Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
 	    	sharingIntent.setType("text/plain");
 			WebView myWebView = (WebView) findViewById(R.id.webview);
 	    	String shareBody = myWebView.getUrl();
 	    	sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this URL");
 	    	sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
 	    	startActivity(Intent.createChooser(sharingIntent, "Share via"));
 	    }
 
 		@Override
 		public boolean onDown(MotionEvent e) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 			float dX = e2.getX()-e1.getX();
 			float dY = e1.getY()-e2.getY();
 			if (Math.abs(dY)<SWIPE_MAX_OFF_PATH && Math.abs(velocityX)>=SWIPE_THRESHOLD_VELOCITY && Math.abs(dX)>=SWIPE_MIN_DISTANCE ) {
 				if (dX>0) {
 					share(null);
 				} else {
 					menu(null);
 				}
 				return true;
 			} 
 			return false;
 		}
 
 		@Override
 		public void onLongPress(MotionEvent e) {
 			// TODO Auto-generated method stub
 			menu(null);
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public void onShowPress(MotionEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public boolean onSingleTapUp(MotionEvent e) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 		
 		@Override
 		public void onPause(){
 		  	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		  	SharedPreferences.Editor editor = settings.edit(); 
 		  	editor.putString("new_url1", NEW_URL1); 
 		  	editor.putString("new_url2", NEW_URL2);
 		  	editor.putString("new_url3", NEW_URL3);
 		  	editor.putString("new_button1", BUTTON_1);
 		  	editor.putString("new_button2", BUTTON_2);
 		  	editor.putString("new_button3", BUTTON_3);
 		  	editor.commit();
 		  	
 			Method pause = null;
 	    	// Resumes the webview.
 	    	try {
 	    		pause = WebView.class.getMethod("onPause");
 	    	} catch (SecurityException e) {
 	    		// Nothing
 	    	} catch (NoSuchMethodException e) {
 	    		// Nothing
 	    	}	if (pause != null) {
 	    		try {
 	    			pause.invoke(myWebView);
 	    		} catch (InvocationTargetException e) {
 				} catch (IllegalAccessException e) {
 				}
 	    	} else {
 	    		// No such method.  Stores the current URL.
 	    		suspendUrl = myWebView.getUrl();
 	    		// And loads a URL without any processing.
 	    		myWebView.loadUrl("");
 	    	}
 	    	
 		  	super.onPause();
 		}
 		
 		@Override
 		public void onStop(){
 		  	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		  	SharedPreferences.Editor editor = settings.edit(); 
 		  	editor.putString("new_url1", NEW_URL1); 
 		  	editor.putString("new_url2", NEW_URL2);
 		  	editor.putString("new_url3", NEW_URL3);
 		  	editor.putString("new_button1", BUTTON_1);
 		  	editor.putString("new_button2", BUTTON_2);
 		  	editor.putString("new_button3", BUTTON_3);
 		  	editor.commit();
 		  	super.onPause();
 		}
 		
 		public class myWebClient extends WebViewClient
 		    {
 		     @Override
 		     public void onPageStarted(WebView view, String url, Bitmap favicon) {
 		      // TODO Auto-generated method stub
 			  progressBar.setVisibility(View.VISIBLE);
 		      super.onPageStarted(view, url, favicon);
 		     }
 		     @Override
 		     public boolean shouldOverrideUrlLoading(WebView view, String url) {
 		      // TODO Auto-generated method stub
 		      view.loadUrl(url);
 		      return true;
 		     }
 		     
 		     @Override
 		     public void onPageFinished(WebView view, String url) {
 		      super.onPageFinished(view, url);
 		      progressBar.setVisibility(View.GONE);
 		     }
 		    }
 		    // To handle "Back" key press event for WebView to go back to previous screen.
 		 @Override
 		 public boolean onKeyDown(int keyCode, KeyEvent event)
 		 {
 		  if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
 		   myWebView.goBack();
 		   return true;
 		  }
 		  return super.onKeyDown(keyCode, event);
 		 }
 		}
 
 		
 //		
 //		 @Override
 //		  public void onPause() {
 //
 //		    	Method pause = null;
 //		    	// Resumes the webview.
 //		    	try {
 //		    		pause = WebView.class.getMethod("onPause");
 //		    	} catch (SecurityException e) {
 //		    		// Nothing
 //		    	} catch (NoSuchMethodException e) {
 //		    		// Nothing
 //		    	}
 //		if (pause != null) {
 //    		try {
 //    			pause.invoke(myWebView);
 //    		} catch (InvocationTargetException e) {
 //			} catch (IllegalAccessException e) {
 //			}
 //    	} else {
 //    		// No such method.  Stores the current URL.
 //    		suspendUrl = myWebView.getUrl();
 //    		// And loads a URL without any processing.
 //    		myWebView.loadUrl("");
 //    	}
 //    	super.onPause();
 // }  
 	
