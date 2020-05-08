 package com.fj.ota;
 
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.DownloadListener;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 
 public class OTAActivity extends Activity {
 	private WebView mWebView;
 	final Activity activity = this;
 
 	public void onBackPressed (){
 
 	    if (mWebView.isFocused() && mWebView.canGoBack()) {
 	    	mWebView.goBack();       
 	    }
 	    else {
 	            openMyDialog(null);
 
 	    }
 	}
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.webview);
 
 	    final ProgressDialog progressDialog = new ProgressDialog(activity);
 	    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 	    progressDialog.setCancelable(false);
 	    
 	    String RomInc= android.os.Build.VERSION.INCREMENTAL;
 	    String BuildDate= RomInc.substring(13, 21);
 	    mWebView = (WebView) findViewById(R.id.webview);
 	    mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.getSettings().setSupportZoom(true);
         mWebView.getSettings().setBuiltInZoomControls(true);
        String url = "http://www.jdvhosting.com/OTA2/ota.php?ROMID=47&ID=44950871&BuildDate= + BuildDate";
        mWebView.loadUrl(url);
 	    mWebView.setWebViewClient(new HelloWebViewClient() {
 	    	@Override
 		    public void onPageFinished(WebView view, String url) {
 		    super.onPageFinished(view, url);
 		    }
 		    });
 	    mWebView.setDownloadListener(new DownloadListener() {
 	        public void onDownloadStart(String url, String userAgent,
 	                String contentDisposition, String mimetype,
 	                long contentLength) {
 	            Intent intent = new Intent(Intent.ACTION_VIEW);
 	            intent.setData(Uri.parse(url));
 	            startActivity(intent);
 
 	        }
 	    });
 	    mWebView.setWebChromeClient(new WebChromeClient() {
 	        public void onProgressChanged(WebView view, int progress) {
 	            progressDialog.show();
 	            progressDialog.setProgress(0);
 	            activity.setProgress(progress * 1000);
 
 	            progressDialog.incrementProgressBy(progress);
 
 	            if(progress == 100 && progressDialog.isShowing())
 	                progressDialog.dismiss();
 	        }
 	    });
 	}
 	
 	private class HelloWebViewClient extends WebViewClient {
 	    @Override
 	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
 	        view.loadUrl(url);
 	        return true;
 	    }
 	    
 	}
 	public void openMyDialog(View view) {
 	    showDialog(10);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 	    switch (id) {
 	    case 10:
 	        // Create our AlertDialog
 	        Builder builder = new AlertDialog.Builder(this);
 	        builder.setMessage("Are you sure you want to exit?")
 	                .setCancelable(true)
 	                .setNegativeButton("No",
 	                        new DialogInterface.OnClickListener() {
 
 	                            @Override
 	                            public void onClick(DialogInterface dialog,
 	                                    int which) {
 	                            }
 	                        })
 	                        .setNeutralButton("Donate?",
 	                        new DialogInterface.OnClickListener() {
 	                            @Override
 	                            public void onClick(DialogInterface dialog,
 	                                    int which) {
 	                                // Ends the activity
 	                            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=A2Q62P43GSRQL"));
 	                    		    startActivity(browserIntent);
 	                            }
 	                        })
 	    	                .setPositiveButton("Yes",
 	    	                        new DialogInterface.OnClickListener() {
 	    	                            @Override
 	    	                            public void onClick(DialogInterface dialog,
 	    	                                    int which) {
 	    	                                // Ends the activity
 	    	                                OTAActivity.this.finish();
 	    	                            }
 	    	                        });
 
 	        return builder.create();
 
 
 	    }
 	    return super.onCreateDialog(id);
 	}
 
   /*  @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Check if the key event was the Back button and if there's history
         if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()){
             mWebView.goBack();
             return true;
         }
         // If it wasn't the Back key or there's no web page history, bubble up to the default
         // system behavior (probably exit the activity)
         return super.onKeyDown(keyCode, event);
         
     } */
 	
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.layout.menu, menu);
 	    return true;
 	}
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.refresh_web:
 	    	mWebView.reload();
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	    
     }
 }
