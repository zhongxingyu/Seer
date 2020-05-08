 package be.iminds.mix.streamstore;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
import android.widget.Toast;
 
 public class MainActivity extends Activity {
     WebView myWebView;
     MyProgressDialog dialog;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         myWebView = (WebView) findViewById(R.id.webview);
         WebSettings webSettings = myWebView.getSettings();
         webSettings.setJavaScriptEnabled(true);
         myWebView.loadUrl("http://straalstroom.mixlab.be");
         myWebView.setWebViewClient(new WebViewClient(){
             @Override
             public boolean shouldOverrideUrlLoading(WebView wv, String url){
                 dialog = MyProgressDialog.show(MainActivity.this, null, null, true, false, null);
                 wv.loadUrl(url);
                 return true;
             }
 
             @Override
             public void onPageFinished(WebView wv, String url){
                 dialog.dismiss();
             }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                Toast.makeText(MainActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
         });
     }
 
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if(event.getAction() == KeyEvent.ACTION_DOWN){
             switch(keyCode)
             {
                 case KeyEvent.KEYCODE_BACK:
                     if(myWebView.canGoBack() == true){
                         myWebView.goBack();
                     }else{
                         finish();
                     }
                     return true;
             }
 
         }
         return super.onKeyDown(keyCode, event);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
