 package com.woshipike.mywebview;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.dom4j.DocumentException;
 
 import com.woshipike.utils.XMLReader;
 import com.woshipike.widget.ScrollWebview;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.R.integer;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.util.TimeUtils;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.animation.TranslateAnimation;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.Scroller;
 import android.widget.TextView;
 
 public class CustomizedWebview extends Activity {
 	
 	private TextView URL;
 	private Button go;
 	private ScrollWebview webView;
 	private Context context;
 	private ArrayList<String> list;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().requestFeature(Window.FEATURE_PROGRESS);
 		setContentView(R.layout.activity_customized_webview);
 		UIInit();
 		WebviewConf();
 		
 		try {
 			XMLReader reader=new XMLReader(context, "scrolltest");
 			list=reader.parse();
 		} catch (DocumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	public void UIInit(){
 		URL=(TextView)findViewById(R.id.editText1);
 		go=(Button)findViewById(R.id.button1);
 		webView=(ScrollWebview)findViewById(R.id.webView1);
 		context=CustomizedWebview.this;
 		
 		go.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
				//String url=URL.getText().toString();
				String url="http://www.jd.com";
 				webView.loadUrl(url);
 				
 			}
 			
 		});
 	}
 	
 	public void WebviewConf(){
 		
 		webView.getSettings().setJavaScriptEnabled(true);
 		
 		webView.setWebViewClient(new WebViewClient(){
 
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url) {
 				// TODO Auto-generated method stub
 				view.loadUrl(url);
 				return true;
 			}
 			
 			public void onPageFinished(WebView view, String url) {
 		        super.onPageFinished(view, url);
 		    } 			
 		});
 		
 		webView.setWebChromeClient(new WebChromeClient(){
 			public void onProgressChanged(WebView view, int progress) {
 			     // Activities and WebViews measure progress with different scales.
 			     // The progress meter will automatically disappear when we reach 100%
 			     Log.v("webview",progress+"");
 			     if(progress==100){
 
 				      new Thread(new scrollTask(new MyHandler())).start();
 			     }
 			   }
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) { 
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.customized_webview, menu);
 		return true;
 	}
 	
 	// handle scroll event
 	private class MyHandler extends Handler{
 
 		@Override
 		public void handleMessage(Message msg) {
 			// TODO Auto-generated method stub
 			super.handleMessage(msg);
 			int S=msg.arg1;
 			int V=msg.arg2;
 			Scroller scroller=webView.getScroller();
 			Log.v("webview", scroller.getFinalX()+", "+scroller.getFinalY());
 			Log.v("webview", scroller.getCurrX()+", "+scroller.getCurrY());
 
 			int startx=scroller.getFinalX();
 			int starty=scroller.getFinalY();
 		    webView.settag();
 		    Log.v("webview", startx+", "+starty+", "+startx+", "+(starty+S)+", "+Math.abs(S*1000/V));
 		    scroller.startScroll(startx, starty, 0, S, Math.abs(S*1000/V));
 		    webView.postInvalidate();
 		}
 		
 	}
 	
 	private class scrollTask implements Runnable {
 		private Handler myhandler;
 		
 		public scrollTask(Handler mHandler){
 			myhandler=mHandler;
 		}
 		
 		public void run() {
 			
 			for (int i=0;i<list.size();i++){
 				Log.v("webview", System.currentTimeMillis()+"");
 				
 				int S=Integer.parseInt(list.get(i).split(",")[0]);
 				int T=Integer.parseInt(list.get(i).split(",")[1]);
 				int V=Integer.parseInt(list.get(i).split(",")[2]);
 
 				
 				Message msg=Message.obtain();
 				msg.what=0;
 				msg.arg1=S;
 				msg.arg2=V;
 				myhandler.sendMessage(msg);
 				
 				try {
 					
 					Thread.sleep(T);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 				
 		}
 	};
 
 }
